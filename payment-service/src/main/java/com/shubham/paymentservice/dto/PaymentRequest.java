package com.shubham.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PaymentRequest {

    @NotNull(message = "Order id is required")
    @Positive(message = "Order id must be positive")
    private Long orderId;

    private BigDecimal amount;

    public PaymentRequest() {}
    public PaymentRequest(Long orderId, BigDecimal amount) {
        this.orderId = orderId; this.amount = amount;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}