package com.shubham.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String method;
    private String razorpayOrderId;
    private String paymentId;
    private String keyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentResponse() {}
    public PaymentResponse(Long id, Long orderId, Long userId, BigDecimal amount, String currency, String status,
                           String method, String razorpayOrderId, String paymentId, String keyId,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.orderId = orderId; this.userId = userId; this.amount = amount; this.currency = currency;
        this.status = status; this.method = method; this.razorpayOrderId = razorpayOrderId;
        this.paymentId = paymentId; this.keyId = keyId; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static PaymentResponseBuilder builder() { return new PaymentResponseBuilder(); }

    public static class PaymentResponseBuilder {
        private Long id; private Long orderId; private Long userId; private BigDecimal amount; private String currency;
        private String status; private String method; private String razorpayOrderId; private String paymentId;
        private String keyId; private LocalDateTime createdAt; private LocalDateTime updatedAt;
        PaymentResponseBuilder() {}
        public PaymentResponseBuilder id(Long id) { this.id = id; return this; }
        public PaymentResponseBuilder orderId(Long orderId) { this.orderId = orderId; return this; }
        public PaymentResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public PaymentResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentResponseBuilder currency(String currency) { this.currency = currency; return this; }
        public PaymentResponseBuilder status(String status) { this.status = status; return this; }
        public PaymentResponseBuilder method(String method) { this.method = method; return this; }
        public PaymentResponseBuilder razorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; return this; }
        public PaymentResponseBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public PaymentResponseBuilder keyId(String keyId) { this.keyId = keyId; return this; }
        public PaymentResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PaymentResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public PaymentResponse build() { return new PaymentResponse(id, orderId, userId, amount, currency, status,
                method, razorpayOrderId, paymentId, keyId, createdAt, updatedAt); }
    }
}