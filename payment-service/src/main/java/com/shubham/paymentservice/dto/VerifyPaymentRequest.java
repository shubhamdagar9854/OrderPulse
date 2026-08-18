package com.shubham.paymentservice.dto;

public class VerifyPaymentRequest {
    private String razorpayOrderId;
    private String paymentId;
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