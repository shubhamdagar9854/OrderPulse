package com.shubham.paymentservice.dto;

public class CreateRazorpayRequest {
    private Long orderId;

    public CreateRazorpayRequest() {}
    public CreateRazorpayRequest(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}