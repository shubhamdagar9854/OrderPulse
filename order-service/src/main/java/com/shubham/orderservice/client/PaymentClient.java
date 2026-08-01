package com.shubham.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "payment-service", url = "${payment-service.url:http://localhost:8084}")
public interface PaymentClient {

    @PostMapping("/api/payments/refund")
    Map<String, Object> refund(@RequestBody Map<String, Object> body);
}
