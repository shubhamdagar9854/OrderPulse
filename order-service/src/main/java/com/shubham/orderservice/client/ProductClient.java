package com.shubham.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "product-service", url = "${product-service.url:http://localhost:8082}")
public interface ProductClient {

    @PutMapping("/api/products/{id}/reduce")
    Map<String, Object> reduceStock(@PathVariable Long id, @RequestBody Map<String, Integer> body);

    @PutMapping("/api/products/{id}/restore")
    Map<String, Object> restoreStock(@PathVariable Long id, @RequestBody Map<String, Integer> body);
}
