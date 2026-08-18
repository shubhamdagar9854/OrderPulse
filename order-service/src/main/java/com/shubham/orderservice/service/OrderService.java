package com.shubham.orderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.orderservice.client.PaymentClient;
import com.shubham.orderservice.client.ProductClient;
import com.shubham.orderservice.dto.OrderRequest;
import com.shubham.orderservice.dto.OrderResponse;
import com.shubham.orderservice.dto.OrderStatusRequest;
import com.shubham.orderservice.entity.Order;
import com.shubham.orderservice.enums.OrderStatus;
import com.shubham.orderservice.exception.ResourceNotFoundException;
import com.shubham.orderservice.exception.ServiceUnavailableException;
import com.shubham.orderservice.repository.OrderRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Map<String, Object> product;
        try {
            product = productClient.reduceStock(request.getProductId(), Map.of("quantity", request.getQuantity()));
        } catch (FeignException.BadRequest e) {
            throw new IllegalArgumentException(extractFeignMessage(e, "Invalid product request"));
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Product not found: " + request.getProductId());
        }

        BigDecimal price = new BigDecimal(product.get("finalPrice").toString());
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .userId(request.getUserId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .build();
        order = orderRepository.save(order);

        publishEvent(order, order.getStatus());

        log.info("Order created: {} | status: {}", order.getId(), order.getStatus());
        return toResponse(order);
    }

    public OrderResponse createOrderFallback(OrderRequest request, Throwable t) {
        log.error("Order creation failed after circuit breaker: {}", t.getMessage());
        throw unwrapOrGeneric("Service", t);
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
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled order cannot be paid");
        }
        if (order.getStatus() == OrderStatus.PAID || order.getStatus().timelinePosition() > OrderStatus.PAID.timelinePosition()) {
            return toResponse(order);
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.touch();
        order = orderRepository.save(order);

        publishEvent(order, OrderStatus.PAID);

        log.info("Order marked PAID: {}", id);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse advanceStatus(Long id, OrderStatusRequest request) {
        OrderStatus target = parseStatus(request.getStatus());

        if (target == OrderStatus.CANCELLED) {
            return cancelOrder(id);
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (order.getStatus() == target) {
            return toResponse(order);
        }
        if (!order.getStatus().canTransitionTo(target)) {
            throw new IllegalArgumentException(
                    "Cannot change order " + id + " from " + order.getStatus() + " to " + target);
        }

        order.setStatus(target);
        applyTimestamp(order, target);
        order.touch();
        order = orderRepository.save(order);

        publishEvent(order, target);

        log.info("Order {} moved to {}", id, target);
        return toResponse(order);
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "cancelOrderFallback")
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order already cancelled");
        }
        if (!order.getStatus().isCancellable()) {
            throw new IllegalArgumentException("Order in " + order.getStatus() + " status cannot be cancelled");
        }

        if (order.getStatus() != OrderStatus.UNPAID) {
            paymentClient.refund(Map.of("orderId", order.getId()));
            log.info("Refund processed for order: {}", order.getId());
        }

        productClient.restoreStock(order.getProductId(), Map.of("quantity", order.getQuantity()));

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.touch();
        order = orderRepository.save(order);

        publishEvent(order, OrderStatus.CANCELLED);

        log.info("Order cancelled: {}", id);
        return toResponse(order);
    }

    public OrderResponse cancelOrderFallback(Long id, Throwable t) {
        log.error("Order cancellation failed after circuit breaker for {}: {}", id, t.getMessage());
        throw unwrapOrGeneric("Cancellation", t);
    }

    private OrderStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        if (!OrderStatus.isValid(value)) {
            throw new IllegalArgumentException("Invalid status: " + value);
        }
        return OrderStatus.valueOf(value.toUpperCase());
    }

    private void applyTimestamp(Order order, OrderStatus status) {
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case PAID -> order.setPaidAt(now);
            case CONFIRMED -> order.setConfirmedAt(now);
            case PROCESSING -> order.setProcessedAt(now);
            case SHIPPED -> order.setShippedAt(now);
            case DELIVERED -> order.setDeliveredAt(now);
            default -> { }
        }
    }

    private void publishEvent(Order order, OrderStatus status) {
        try {
            kafkaTemplate.send("order-events", Map.of(
                    "orderId", order.getId(),
                    "userId", order.getUserId(),
                    "status", status.name()
            ));
        } catch (Exception e) {
            log.warn("Kafka not available, order event skipped: {}", e.getMessage());
        }
    }

    private RuntimeException unwrapOrGeneric(String context, Throwable t) {
        if (t instanceof ResourceNotFoundException || t instanceof IllegalArgumentException) {
            return (RuntimeException) t;
        }
        if (t instanceof FeignException feign) {
            return new ServiceUnavailableException(extractFeignMessage(feign, context + " temporarily unavailable. Please try again."));
        }
        return new ServiceUnavailableException(context + " temporarily unavailable. Please try again.");
    }

    private String extractFeignMessage(FeignException e, String fallback) {
        try {
            var body = e.responseBody();
            if (body.isPresent()) {
                String json = StandardCharsets.UTF_8.decode(body.get()).toString();
                Map<String, Object> parsed = new ObjectMapper().readValue(json, Map.class);
                if (parsed.get("message") != null) {
                    return parsed.get("message").toString();
                }
            }
        } catch (Exception ignored) {
            // fall through to fallback message
        }
        return fallback;
    }

    private OrderResponse toResponse(Order order) {
        Map<OrderStatus, LocalDateTime> steps = new LinkedHashMap<>();
        steps.put(OrderStatus.UNPAID, order.getCreatedAt());
        if (order.getPaidAt() != null) steps.put(OrderStatus.PAID, order.getPaidAt());
        if (order.getConfirmedAt() != null) steps.put(OrderStatus.CONFIRMED, order.getConfirmedAt());
        if (order.getProcessedAt() != null) steps.put(OrderStatus.PROCESSING, order.getProcessedAt());
        if (order.getShippedAt() != null) steps.put(OrderStatus.SHIPPED, order.getShippedAt());
        if (order.getDeliveredAt() != null) steps.put(OrderStatus.DELIVERED, order.getDeliveredAt());
        if (order.getCancelledAt() != null) steps.put(OrderStatus.CANCELLED, order.getCancelledAt());

        List<OrderResponse.TimelineEntry> timeline = new ArrayList<>();
        for (Map.Entry<OrderStatus, LocalDateTime> entry : steps.entrySet()) {
            timeline.add(new OrderResponse.TimelineEntry(entry.getKey().name(), entry.getKey().label(), entry.getValue()));
        }

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .statusLabel(order.getStatus().label())
                .createdAt(order.getCreatedAt())
                .build()
                .withTimestamps(order.getPaidAt(), order.getConfirmedAt(), order.getProcessedAt(),
                        order.getShippedAt(), order.getDeliveredAt(), order.getCancelledAt(), order.getUpdatedAt(), timeline);
    }
}