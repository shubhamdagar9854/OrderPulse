package com.shubham.paymentservice.service;

import com.shubham.paymentservice.client.OrderClient;
import com.shubham.paymentservice.dto.PaymentRequest;
import com.shubham.paymentservice.dto.PaymentResponse;
import com.shubham.paymentservice.entity.Payment;
import com.shubham.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderClient orderClient;

    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate,
                          OrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.orderClient = orderClient;
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        Map<String, Object> order = orderClient.getOrder(request.getOrderId());
        BigDecimal orderTotal = new BigDecimal(order.get("totalPrice").toString());
        if (request.getAmount().compareTo(orderTotal) != 0) {
            throw new IllegalArgumentException("Amount mismatch. Expected: " + orderTotal);
        }

        orderClient.markPaid(request.getOrderId());

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .build();
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

        log.info("Payment processed: {}", payment.getId());
        return toResponse(payment);
    }

    public List<PaymentResponse> findAll() {
        return paymentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PaymentResponse refund(Long orderId) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .status("REFUNDED")
                .build();
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

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
