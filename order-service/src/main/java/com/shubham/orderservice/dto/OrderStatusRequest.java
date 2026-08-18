package com.shubham.orderservice.dto;

public class OrderStatusRequest {
    private String status;

    public OrderStatusRequest() {}
    public OrderStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}