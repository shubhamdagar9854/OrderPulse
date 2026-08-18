package com.shubham.paymentservice.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import com.razorpay.Utils;
import com.shubham.paymentservice.client.OrderClient;
import com.shubham.paymentservice.dto.CreateRazorpayRequest;
import com.shubham.paymentservice.dto.PaymentResponse;
import com.shubham.paymentservice.dto.VerifyPaymentRequest;
import com.shubham.paymentservice.entity.Payment;
import com.shubham.paymentservice.exception.ResourceNotFoundException;
import com.shubham.paymentservice.repository.PaymentRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String CURRENCY = "INR";

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderClient orderClient;
    private final RazorpayClient razorpayClient;
    private final String keySecret;
    private final String keyId;

    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate,
                          OrderClient orderClient,
                          @Value("${razorpay.key.id}") String keyId,
                          @Value("${razorpay.key.secret}") String keySecret)
            throws RazorpayException {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.orderClient = orderClient;
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
        this.keySecret = keySecret;
        this.keyId = keyId;
    }

    public PaymentResponse createRazorpayOrder(CreateRazorpayRequest request, Long userId) {
        Map<String, Object> order;
        try {
            order = orderClient.getOrder(request.getOrderId());
        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("Order not found with id: " + request.getOrderId());
        }

        String orderStatus = order.get("status").toString();
        if ("PAID".equals(orderStatus) || "CANCELLED".equals(orderStatus)) {
            throw new IllegalArgumentException("Order is already " + orderStatus);
        }

        BigDecimal orderTotal = new BigDecimal(order.get("totalPrice").toString());
        int amountInPaise = orderTotal.multiply(BigDecimal.valueOf(100)).intValue();

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", CURRENCY);
            orderRequest.put("receipt", "order_" + request.getOrderId() + "_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);

            Order rzpOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = rzpOrder.get("id");

            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .userId(userId)
                    .razorpayOrderId(razorpayOrderId)
                    .amount(orderTotal)
                    .currency(CURRENCY)
                    .status("PENDING")
                    .build();
            payment = paymentRepository.save(payment);
            log.info("Razorpay order created: {} for order {}", razorpayOrderId, request.getOrderId());
            return toResponse(payment);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for order {}: {}", request.getOrderId(), e.getMessage());
            throw new IllegalStateException("Could not initiate payment. Check Razorpay keys and try again.");
        }
    }

    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for razorpay order: " + request.getRazorpayOrderId()));

        if ("SUCCESS".equals(payment.getStatus()) || "REFUNDED".equals(payment.getStatus())) {
            return toResponse(payment);
        }

        String payload = request.getRazorpayOrderId() + "|" + request.getPaymentId();
        boolean valid;
        try {
            valid = Utils.verifySignature(payload, request.getSignature(), keySecret);
        } catch (Exception e) {
            valid = false;
        }

        if (!valid) {
            payment.setStatus("FAILED");
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            log.error("Signature verification FAILED for razorpay order {}", request.getRazorpayOrderId());
            throw new IllegalArgumentException("Payment verification failed. Signature mismatch.");
        }

        orderClient.markPaid(payment.getOrderId());
        payment.setPaymentId(request.getPaymentId());
        payment.setSignature(request.getSignature());
        payment.setStatus("SUCCESS");
        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        try {
            kafkaTemplate.send("payment-events", Map.of(
                    "paymentId", payment.getId(),
                    "orderId", payment.getOrderId(),
                    "status", payment.getStatus()
            ));
        } catch (Exception e) {
            log.warn("Kafka not available, payment event skipped: {}", e.getMessage());
        }

        log.info("Payment verified & marked SUCCESS: {} for order {}", payment.getId(), payment.getOrderId());
        return toResponse(payment);
    }

    public PaymentResponse refund(Long orderId) {
        Payment payment = paymentRepository.findTopByOrderIdAndStatusOrderByUpdatedAtDesc(orderId, "SUCCESS")
                .orElseThrow(() -> new IllegalArgumentException("No successful payment found for order: " + orderId));

        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("payment_id", payment.getPaymentId());
            Refund refund = razorpayClient.refunds.create(refundRequest);
            log.info("Razorpay refund processed: {} for payment {}", refund.get("id"), payment.getPaymentId());
        } catch (RazorpayException e) {
            log.error("Razorpay refund failed for order {}: {}", orderId, e.getMessage());
            throw new IllegalStateException("Refund failed. Please try again.");
        }

        payment.setStatus("REFUNDED");
        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        try {
            kafkaTemplate.send("payment-events", Map.of(
                    "paymentId", payment.getId(),
                    "orderId", orderId,
                    "status", "REFUNDED"
            ));
        } catch (Exception e) {
            log.warn("Kafka not available, refund event skipped: {}", e.getMessage());
        }

        log.info("Payment refunded for order: {}", orderId);
        return toResponse(payment);
    }

    public List<PaymentResponse> findAll() {
        return paymentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .paymentId(payment.getPaymentId())
                .keyId(keyId)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}