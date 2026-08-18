package com.shubham.paymentservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Long userId;

    private String razorpayOrderId;

    private String paymentId;

    private String signature;

    private String method;

    private BigDecimal amount;

    private String currency = "INR";

    private String status = "PENDING";

    @Column(columnDefinition = "datetime")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "datetime")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Payment() {}

    public Payment(Long id, Long orderId, Long userId, String razorpayOrderId, String paymentId,
                   String signature, String method, BigDecimal amount, String currency, String status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.razorpayOrderId = razorpayOrderId;
        this.paymentId = paymentId;
        this.signature = signature;
        this.method = method;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static PaymentBuilder builder() { return new PaymentBuilder(); }

    public static class PaymentBuilder {
        private Long id; private Long orderId; private Long userId; private String razorpayOrderId;
        private String paymentId; private String signature; private String method; private BigDecimal amount;
        private String currency = "INR"; private String status = "PENDING";
        private LocalDateTime createdAt = LocalDateTime.now(); private LocalDateTime updatedAt = LocalDateTime.now();
        PaymentBuilder() {}
        public PaymentBuilder id(Long id) { this.id = id; return this; }
        public PaymentBuilder orderId(Long orderId) { this.orderId = orderId; return this; }
        public PaymentBuilder userId(Long userId) { this.userId = userId; return this; }
        public PaymentBuilder razorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; return this; }
        public PaymentBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public PaymentBuilder signature(String signature) { this.signature = signature; return this; }
        public PaymentBuilder method(String method) { this.method = method; return this; }
        public PaymentBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentBuilder currency(String currency) { this.currency = currency; return this; }
        public PaymentBuilder status(String status) { this.status = status; return this; }
        public PaymentBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PaymentBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Payment build() { return new Payment(id, orderId, userId, razorpayOrderId, paymentId,
                signature, method, amount, currency, status, createdAt, updatedAt); }
    }
}