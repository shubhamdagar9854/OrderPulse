package com.shubham.paymentservice.service;

import com.shubham.paymentservice.client.OrderClient;
import com.shubham.paymentservice.dto.PaymentResponse;
import com.shubham.paymentservice.dto.VerifyPaymentRequest;
import com.shubham.paymentservice.entity.Payment;
import com.shubham.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private static final String KEY_ID = "rzp_test_xxxxxxxx";
    private static final String KEY_SECRET = "test_secret_key";

    private PaymentRepository paymentRepository;
    private OrderClient orderClient;
    private KafkaTemplate<String, Object> kafkaTemplate;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() throws Exception {
        paymentRepository = mock(PaymentRepository.class);
        orderClient = mock(OrderClient.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        paymentService = new PaymentService(paymentRepository, kafkaTemplate, orderClient, KEY_ID, KEY_SECRET);
    }

    private Payment payment(String rzpOrderId) {
        return Payment.builder()
                .id(1L)
                .orderId(100L)
                .userId(7L)
                .razorpayOrderId(rzpOrderId)
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .status("PENDING")
                .build();
    }

    private String computeSignature(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(KEY_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void verifyPayment_tamperedSignature_marksFailedAndThrows() {
        Payment payment = payment("rzp_order_1");
        when(paymentRepository.findByRazorpayOrderId("rzp_order_1")).thenReturn(Optional.of(payment));

        VerifyPaymentRequest request = new VerifyPaymentRequest("rzp_order_1", "pay_1", "invalid_signature");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.verifyPayment(request));
        assertTrue(ex.getMessage().contains("Signature mismatch"));
        assertEquals("FAILED", payment.getStatus());
        verify(paymentRepository).save(payment);
        verify(orderClient, never()).markPaid(any(Long.class));
    }

    @Test
    void verifyPayment_validSignature_marksSuccessAndCallsMarkPaid() throws Exception {
        Payment payment = payment("rzp_order_2");
        when(paymentRepository.findByRazorpayOrderId("rzp_order_2")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderClient.markPaid(100L)).thenReturn(Map.of());

        String payload = "rzp_order_2|pay_2";
        String signature = computeSignature(payload);

        VerifyPaymentRequest request = new VerifyPaymentRequest("rzp_order_2", "pay_2", signature);
        PaymentResponse response = paymentService.verifyPayment(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("pay_2", payment.getPaymentId());
        assertEquals("pay_2", response.getPaymentId());
        verify(orderClient).markPaid(100L);
        verify(kafkaTemplate, atLeastOnce()).send(eq("payment-events"), any(Map.class));
    }

    @Test
    void verifyPayment_unknownRazorpayOrder_throwsNotFound() {
        when(paymentRepository.findByRazorpayOrderId("nope")).thenReturn(Optional.empty());

        VerifyPaymentRequest request = new VerifyPaymentRequest("nope", "pay_1", "sig");
        assertThrows(RuntimeException.class, () -> paymentService.verifyPayment(request));
    }

    @Test
    void refund_noSuccessfulPayment_throwsIllegalArgument() {
        when(paymentRepository.findTopByOrderIdAndStatusOrderByUpdatedAtDesc(100L, "SUCCESS"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> paymentService.refund(100L));
    }
}