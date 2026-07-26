package com.shubham.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    public PaymentResponse() {}
    public PaymentResponse(Long id, Long orderId, BigDecimal amount, String status, LocalDateTime createdAt) {
        this.id = id; this.orderId = orderId; this.amount = amount; this.status = status; this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static PaymentResponseBuilder builder() { return new PaymentResponseBuilder(); }

    public static class PaymentResponseBuilder {
        private Long id; private Long orderId; private BigDecimal amount; private String status; private LocalDateTime createdAt;
        PaymentResponseBuilder() {}
        public PaymentResponseBuilder id(Long id) { this.id = id; return this; }
        public PaymentResponseBuilder orderId(Long orderId) { this.orderId = orderId; return this; }
        public PaymentResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentResponseBuilder status(String status) { this.status = status; return this; }
        public PaymentResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PaymentResponse build() { return new PaymentResponse(id, orderId, amount, status, createdAt); }
    }
}
