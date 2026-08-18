package com.shubham.productservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private String brand;
    private String category;
    private BigDecimal price;
    private BigDecimal discountPercent;
    private BigDecimal finalPrice;
    private Integer quantity;
    private Integer lowStockThreshold;
    private Double rating;
    private String status;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse() {}
    public ProductResponse(Long id, String sku, String name, String description, String brand, String category,
                           BigDecimal price, BigDecimal discountPercent, BigDecimal finalPrice, Integer quantity,
                           Integer lowStockThreshold, Double rating, String status, String imageUrl,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.sku = sku; this.name = name; this.description = description; this.brand = brand;
        this.category = category; this.price = price; this.discountPercent = discountPercent; this.finalPrice = finalPrice;
        this.quantity = quantity; this.lowStockThreshold = lowStockThreshold; this.rating = rating; this.status = status;
        this.imageUrl = imageUrl; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static ProductResponseBuilder builder() { return new ProductResponseBuilder(); }

    public static class ProductResponseBuilder {
        private Long id; private String sku; private String name; private String description; private String brand;
        private String category; private BigDecimal price; private BigDecimal discountPercent; private BigDecimal finalPrice;
        private Integer quantity; private Integer lowStockThreshold; private Double rating; private String status;
        private String imageUrl; private LocalDateTime createdAt; private LocalDateTime updatedAt;
        ProductResponseBuilder() {}
        public ProductResponseBuilder id(Long id) { this.id = id; return this; }
        public ProductResponseBuilder sku(String sku) { this.sku = sku; return this; }
        public ProductResponseBuilder name(String name) { this.name = name; return this; }
        public ProductResponseBuilder description(String description) { this.description = description; return this; }
        public ProductResponseBuilder brand(String brand) { this.brand = brand; return this; }
        public ProductResponseBuilder category(String category) { this.category = category; return this; }
        public ProductResponseBuilder price(BigDecimal price) { this.price = price; return this; }
        public ProductResponseBuilder discountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; return this; }
        public ProductResponseBuilder finalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; return this; }
        public ProductResponseBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public ProductResponseBuilder lowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; return this; }
        public ProductResponseBuilder rating(Double rating) { this.rating = rating; return this; }
        public ProductResponseBuilder status(String status) { this.status = status; return this; }
        public ProductResponseBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public ProductResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ProductResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public ProductResponse build() { return new ProductResponse(id, sku, name, description, brand, category, price,
                discountPercent, finalPrice, quantity, lowStockThreshold, rating, status, imageUrl, createdAt, updatedAt); }
    }
}