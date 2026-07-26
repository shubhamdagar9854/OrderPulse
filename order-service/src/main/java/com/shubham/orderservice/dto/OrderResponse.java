package com.shubham.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;

    public OrderResponse() {}
    public OrderResponse(Long id, Long userId, Long productId, Integer quantity, BigDecimal totalPrice, String status, LocalDateTime createdAt) {
        this.id = id; this.userId = userId; this.productId = productId; this.quantity = quantity;
        this.totalPrice = totalPrice; this.status = status; this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static OrderResponseBuilder builder() { return new OrderResponseBuilder(); }

    public static class OrderResponseBuilder {
        private Long id; private Long userId; private Long productId; private Integer quantity;
        private BigDecimal totalPrice; private String status; private LocalDateTime createdAt;
        OrderResponseBuilder() {}
        public OrderResponseBuilder id(Long id) { this.id = id; return this; }
        public OrderResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public OrderResponseBuilder productId(Long productId) { this.productId = productId; return this; }
        public OrderResponseBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderResponseBuilder totalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; return this; }
        public OrderResponseBuilder status(String status) { this.status = status; return this; }
        public OrderResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderResponse build() { return new OrderResponse(id, userId, productId, quantity, totalPrice, status, createdAt); }
    }
}
