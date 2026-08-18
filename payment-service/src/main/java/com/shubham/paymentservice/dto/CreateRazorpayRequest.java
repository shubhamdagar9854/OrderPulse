package com.shubham.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateRazorpayRequest {

    @NotNull(message = "Order id is required")
    @Positive(message = "Order id must be positive")
    private Long orderId;

    public CreateRazorpayRequest() {}
    public CreateRazorpayRequest(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}