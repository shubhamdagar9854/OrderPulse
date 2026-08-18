package com.shubham.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AnalyticsResponse {

    private long totalOrders;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private long ordersToday;
    private BigDecimal averageOrderValue = BigDecimal.ZERO;
    private Map<String, Long> orderStatusCounts;
    private List<TopProduct> topProducts;
    private List<OrderResponse> recentOrders;

    public static class TopProduct {
        private Long productId;
        private String productName;
        private Long totalQuantity;

        public TopProduct() {}

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Long getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(Long totalQuantity) { this.totalQuantity = totalQuantity; }
    }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getOrdersToday() { return ordersToday; }
    public void setOrdersToday(long ordersToday) { this.ordersToday = ordersToday; }
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    public Map<String, Long> getOrderStatusCounts() { return orderStatusCounts; }
    public void setOrderStatusCounts(Map<String, Long> orderStatusCounts) { this.orderStatusCounts = orderStatusCounts; }
    public List<TopProduct> getTopProducts() { return topProducts; }
    public void setTopProducts(List<TopProduct> topProducts) { this.topProducts = topProducts; }
    public List<OrderResponse> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<OrderResponse> recentOrders) { this.recentOrders = recentOrders; }
}