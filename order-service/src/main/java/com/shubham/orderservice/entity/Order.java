package com.shubham.orderservice.entity;

import com.shubham.orderservice.enums.OrderStatus;
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

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.UNPAID;

    @Column(columnDefinition = "datetime")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "datetime")
    private LocalDateTime paidAt;

    @Column(columnDefinition = "datetime")
    private LocalDateTime confirmedAt;

    @Column(columnDefinition = "datetime")
    private LocalDateTime processedAt;

    @Column(columnDefinition = "datetime")
    private LocalDateTime shippedAt;

    @Column(columnDefinition = "datetime")
    private LocalDateTime deliveredAt;

    @Column(columnDefinition = "datetime")
    private LocalDateTime cancelledAt;

    @Column(columnDefinition = "datetime")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Order() {}

    public Order(Long id, Long userId, Long productId, Integer quantity, BigDecimal totalPrice, OrderStatus status, LocalDateTime createdAt) {
        this.id = id; this.userId = userId; this.productId = productId; this.quantity = quantity;
        this.totalPrice = totalPrice; this.status = status; this.createdAt = createdAt;
        this.updatedAt = createdAt;
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
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public static OrderBuilder builder() { return new OrderBuilder(); }

    public static class OrderBuilder {
        private Long id; private Long userId; private Long productId; private Integer quantity;
        private BigDecimal totalPrice; private OrderStatus status = OrderStatus.UNPAID; private LocalDateTime createdAt = LocalDateTime.now();
        OrderBuilder() {}
        public OrderBuilder id(Long id) { this.id = id; return this; }
        public OrderBuilder userId(Long userId) { this.userId = userId; return this; }
        public OrderBuilder productId(Long productId) { this.productId = productId; return this; }
        public OrderBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderBuilder totalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; return this; }
        public OrderBuilder status(OrderStatus status) { this.status = status; return this; }
        public OrderBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Order build() { return new Order(id, userId, productId, quantity, totalPrice, status, createdAt); }
    }
}