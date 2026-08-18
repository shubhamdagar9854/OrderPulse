package com.shubham.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyPaymentRequest {

    @NotBlank(message = "Razorpay order id is required")
    private String razorpayOrderId;

    @NotBlank(message = "Payment id is required")
    private String paymentId;

    @NotBlank(message = "Signature is required")
    private String signature;

    public VerifyPaymentRequest() {}
    public VerifyPaymentRequest(String razorpayOrderId, String paymentId, String signature) {
        this.razorpayOrderId = razorpayOrderId;
        this.paymentId = paymentId;
        this.signature = signature;
    }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}