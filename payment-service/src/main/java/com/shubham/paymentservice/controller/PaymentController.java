package com.shubham.paymentservice.controller;

import com.shubham.paymentservice.dto.PaymentRequest;
import com.shubham.paymentservice.dto.PaymentResponse;
import com.shubham.paymentservice.service.PaymentService;
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

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refund(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.refund(request.getOrderId()));
    }
}
