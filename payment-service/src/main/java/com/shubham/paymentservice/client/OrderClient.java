package com.shubham.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Map;

@FeignClient(name = "order-service", url = "${order-service.url:http://localhost:8083}")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    Map<String, Object> getOrder(@PathVariable Long id);

    @PutMapping("/api/orders/{id}/pay")
    Map<String, Object> markPaid(@PathVariable Long id);
}
