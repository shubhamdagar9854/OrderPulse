package com.shubham.orderservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalPrice;

    private String status = "PENDING";

    private LocalDateTime createdAt = LocalDateTime.now();

    public Order() {}
    public Order(Long id, Long userId, Long productId, Integer quantity, BigDecimal totalPrice, String status, LocalDateTime createdAt) {
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

    public static OrderBuilder builder() { return new OrderBuilder(); }

    public static class OrderBuilder {
        private Long id; private Long userId; private Long productId; private Integer quantity;
        private BigDecimal totalPrice; private String status = "PENDING"; private LocalDateTime createdAt = LocalDateTime.now();
        OrderBuilder() {}
        public OrderBuilder id(Long id) { this.id = id; return this; }
        public OrderBuilder userId(Long userId) { this.userId = userId; return this; }
        public OrderBuilder productId(Long productId) { this.productId = productId; return this; }
        public OrderBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderBuilder totalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; return this; }
        public OrderBuilder status(String status) { this.status = status; return this; }
        public OrderBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Order build() { return new Order(id, userId, productId, quantity, totalPrice, status, createdAt); }
    }
}
