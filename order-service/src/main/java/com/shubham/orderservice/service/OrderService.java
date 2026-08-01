package com.shubham.orderservice.service;

import com.shubham.orderservice.client.PaymentClient;
import com.shubham.orderservice.client.ProductClient;
import com.shubham.orderservice.dto.OrderRequest;
import com.shubham.orderservice.dto.OrderResponse;
import com.shubham.orderservice.entity.Order;
import com.shubham.orderservice.exception.ResourceNotFoundException;
import com.shubham.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, ProductClient productClient,
                        PaymentClient paymentClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.paymentClient = paymentClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "createOrderFallback")
    @RateLimiter(name = "createOrder", fallbackMethod = "createOrderFallback")
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Map<String, Object> product = productClient.reduceStock(request.getProductId(), Map.of("quantity", request.getQuantity()));

        BigDecimal price = new BigDecimal(product.get("price").toString());
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .userId(request.getUserId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .build();
        order = orderRepository.save(order);

        try {
            kafkaTemplate.send("order-events", Map.of(
                    "orderId", order.getId(),
                    "userId", order.getUserId(),
                    "productId", order.getProductId(),
                    "quantity", order.getQuantity(),
                    "totalPrice", order.getTotalPrice(),
                    "status", order.getStatus(),
                    "email", "user@example.com"
            ));
        } catch (Exception e) {
            log.warn("Kafka not available, order event skipped: {}", e.getMessage());
        }

        log.info("Order created: {} | status: {}", order.getId(), order.getStatus());
        return toResponse(order);
    }

    public OrderResponse createOrderFallback(OrderRequest request, Throwable t) {
        log.error("Order creation failed after circuit breaker: {}", t.getMessage());
        throw new RuntimeException("Service temporarily unavailable. Please try again.");
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return toResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse markOrderPaid(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if ("PAID".equals(order.getStatus())) {
            return toResponse(order);
        }
        if ("CANCELLED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Cancelled order cannot be paid");
        }

        order.setStatus("PAID");
        order = orderRepository.save(order);

        try {
            kafkaTemplate.send("order-events", Map.of(
                    "orderId", order.getId(),
                    "status", "PAID"
            ));
        } catch (Exception e) {
            log.warn("Kafka not available, paid event skipped: {}", e.getMessage());
        }

        log.info("Order marked PAID: {}", id);
        return toResponse(order);
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "cancelOrderFallback")
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if ("CANCELLED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Order already cancelled");
        }

        if ("PAID".equals(order.getStatus())) {
            paymentClient.refund(Map.of("orderId", order.getId()));
            log.info("Refund processed for order: {}", order.getId());
        }

        productClient.restoreStock(order.getProductId(), Map.of("quantity", order.getQuantity()));

        order.setStatus("CANCELLED");
        order = orderRepository.save(order);

        try {
            kafkaTemplate.send("order-events", Map.of(
                    "orderId", order.getId(),
                    "status", "CANCELLED"
            ));
        } catch (Exception e) {
            log.warn("Kafka not available, cancel event skipped: {}", e.getMessage());
        }

        log.info("Order cancelled: {}", id);
        return toResponse(order);
    }

    public OrderResponse cancelOrderFallback(Long id, Throwable t) {
        log.error("Order cancellation failed after circuit breaker for {}: {}", id, t.getMessage());
        throw new RuntimeException("Cancellation temporarily unavailable. Please try again.");
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
