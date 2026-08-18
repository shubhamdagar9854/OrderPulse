package com.shubham.paymentservice.controller;

import com.shubham.paymentservice.dto.CreateRazorpayRequest;
import com.shubham.paymentservice.dto.PaymentRequest;
import com.shubham.paymentservice.dto.PaymentResponse;
import com.shubham.paymentservice.dto.VerifyPaymentRequest;
import com.shubham.paymentservice.exception.ForbiddenException;
import com.shubham.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/razorpay/order")
    public ResponseEntity<PaymentResponse> createRazorpayOrder(
            @Valid @RequestBody CreateRazorpayRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = (userIdHeader != null && !userIdHeader.isBlank()) ? Long.valueOf(userIdHeader) : null;
        return ResponseEntity.ok(paymentService.createRazorpayOrder(request, userId));
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refund(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.refund(request.getOrderId()));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUser(@PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xRole) {
        if (!"ADMIN".equals(xRole) && !String.valueOf(userId).equals(xUserId)) {
            throw new ForbiddenException("You do not have permission to access this user's payments");
        }
        return ResponseEntity.ok(paymentService.getPaymentsByUserId(userId));
    }
}