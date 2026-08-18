package com.shubham.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private String statusLabel;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime processedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime updatedAt;
    private List<TimelineEntry> timeline;

    public static class TimelineEntry {
        private String status;
        private String label;
        private LocalDateTime timestamp;

        public TimelineEntry(String status, String label, LocalDateTime timestamp) {
            this.status = status;
            this.label = label;
            this.timestamp = timestamp;
        }

        public String getStatus() { return status; }
        public String getLabel() { return label; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public OrderResponse() {}
    public OrderResponse(Long id, Long userId, Long productId, Integer quantity, BigDecimal totalPrice, String status, String statusLabel, LocalDateTime createdAt) {
        this.id = id; this.userId = userId; this.productId = productId; this.quantity = quantity;
        this.totalPrice = totalPrice; this.status = status; this.statusLabel = statusLabel; this.createdAt = createdAt;
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
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
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
    public List<TimelineEntry> getTimeline() { return timeline; }
    public void setTimeline(List<TimelineEntry> timeline) { this.timeline = timeline; }

    public OrderResponse withTimestamps(LocalDateTime paidAt, LocalDateTime confirmedAt, LocalDateTime processedAt,
                                        LocalDateTime shippedAt, LocalDateTime deliveredAt, LocalDateTime cancelledAt,
                                        LocalDateTime updatedAt, List<TimelineEntry> timeline) {
        this.paidAt = paidAt;
        this.confirmedAt = confirmedAt;
        this.processedAt = processedAt;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.cancelledAt = cancelledAt;
        this.updatedAt = updatedAt;
        this.timeline = timeline;
        return this;
    }

    public static OrderResponseBuilder builder() { return new OrderResponseBuilder(); }

    public static class OrderResponseBuilder {
        private Long id; private Long userId; private Long productId; private Integer quantity;
        private BigDecimal totalPrice; private String status; private String statusLabel; private LocalDateTime createdAt;
        OrderResponseBuilder() {}
        public OrderResponseBuilder id(Long id) { this.id = id; return this; }
        public OrderResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public OrderResponseBuilder productId(Long productId) { this.productId = productId; return this; }
        public OrderResponseBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderResponseBuilder totalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; return this; }
        public OrderResponseBuilder status(String status) { this.status = status; return this; }
        public OrderResponseBuilder statusLabel(String statusLabel) { this.statusLabel = statusLabel; return this; }
        public OrderResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderResponse build() { return new OrderResponse(id, userId, productId, quantity, totalPrice, status, statusLabel, createdAt); }
    }
}