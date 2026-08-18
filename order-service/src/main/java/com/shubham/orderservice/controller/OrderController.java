package com.shubham.orderservice.controller;

import com.shubham.orderservice.dto.AnalyticsResponse;
import com.shubham.orderservice.dto.CartItemRequest;
import com.shubham.orderservice.dto.CartItemResponse;
import com.shubham.orderservice.dto.OrderRequest;
import com.shubham.orderservice.dto.OrderResponse;
import com.shubham.orderservice.dto.OrderStatusRequest;
import com.shubham.orderservice.service.CartService;
import com.shubham.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xRole) {
        return ResponseEntity.ok(orderService.getOrderById(id, xUserId, xRole));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xRole) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, xUserId, xRole));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markOrderPaid(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(orderService.advanceStatus(id, request));
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<AnalyticsResponse> analytics() {
        return ResponseEntity.ok(orderService.getAnalytics());
    }

    @GetMapping("/cart/{userId}")
    public ResponseEntity<CartItemResponse> getCart(@PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xRole) {
        return ResponseEntity.ok(cartService.getCart(userId, xUserId, xRole));
    }

    @PostMapping("/cart/{userId}/items")
    public ResponseEntity<CartItemResponse> addToCart(@PathVariable Long userId, @Valid @RequestBody CartItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xRole) {
        return ResponseEntity.ok(cartService.addItem(userId, request, xUserId, xRole));
    }

    @PutMapping("/cart/{userId}/items/{productId}")
    public ResponseEntity<CartItemResponse> updateCartItem(@PathVariable Long userId, @PathVariable Long productId,
            @Valid @RequestBody CartItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xRole) {
        return ResponseEntity.ok(cartService.updateQuantity(userId, productId, request, xUserId, xRole));
    }

    @DeleteMapping("/cart/{userId}/items/{productId}")
    public ResponseEntity<CartItemResponse> removeCartItem(@PathVariable Long userId, @PathVariable Long productId,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xRole) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId, xUserId, xRole));
    }

    @DeleteMapping("/cart/{userId}")
    public ResponseEntity<CartItemResponse> clearCart(@PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xRole) {
        return ResponseEntity.ok(cartService.clear(userId, xUserId, xRole));
    }

    @PostMapping("/cart/{userId}/checkout")
    public ResponseEntity<List<OrderResponse>> checkout(@PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xRole) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.checkout(userId, xUserId, xRole));
    }
}