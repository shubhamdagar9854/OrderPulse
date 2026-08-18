package com.shubham.productservice.service;

import com.shubham.productservice.dto.ProductRequest;
import com.shubham.productservice.dto.ProductResponse;
import com.shubham.productservice.entity.Product;
import com.shubham.productservice.exception.ResourceNotFoundException;
import com.shubham.productservice.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(ProductRequest request) {
        if (request.getSku() != null && productRepository.findBySku(request.getSku()).isPresent()) {
            throw new IllegalArgumentException("SKU already exists: " + request.getSku());
        }
        Product product = new Product();
        applyRequest(product, request);
        product.recalculateFinalPrice();
        product = productRepository.save(product);
        return toResponse(product);
    }

    @Cacheable(value = "products")
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse findById(Long id) {
        return toResponse(getProduct(id));
    }

    public Page<ProductResponse> search(String search, String category, BigDecimal minPrice, BigDecimal maxPrice,
                                        String sort, int page, int size) {
        List<Product> products = productRepository.findAll();
        java.util.stream.Stream<Product> stream = products.stream();

        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            stream = stream.filter(p ->
                    (p.getName() != null && p.getName().toLowerCase().contains(q))
                            || (p.getBrand() != null && p.getBrand().toLowerCase().contains(q))
                            || (p.getSku() != null && p.getSku().toLowerCase().contains(q)));
        }
        if (category != null && !category.isBlank()) {
            stream = stream.filter(p -> category.equalsIgnoreCase(p.getCategory()));
        }
        if (minPrice != null) {
            stream = stream.filter(p -> p.getFinalPrice().compareTo(minPrice) >= 0);
        }
        if (maxPrice != null) {
            stream = stream.filter(p -> p.getFinalPrice().compareTo(maxPrice) <= 0);
        }

        List<Product> filtered = stream.sorted(sortComparator(sort)).toList();
        int total = filtered.size();
        int start = Math.min(page * size, total);
        int end = Math.min(start + size, total);
        List<ProductResponse> content = filtered.subList(start, end).stream()
                .map(this::toResponse)
                .toList();
        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }

    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        applyRequest(product, request);
        product.recalculateFinalPrice();
        product.setUpdatedAt(LocalDateTime.now());
        product = productRepository.save(product);
        return toResponse(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);
        productRepository.delete(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponse reduceStock(Long id, Integer quantity) {
        Product product = getProduct(id);
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getQuantity());
        }
        product.setQuantity(product.getQuantity() - quantity);
        product.setUpdatedAt(LocalDateTime.now());
        product = productRepository.save(product);
        return toResponse(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponse restoreStock(Long id, Integer quantity) {
        Product product = getProduct(id);
        product.setQuantity(product.getQuantity() + quantity);
        product.setUpdatedAt(LocalDateTime.now());
        product = productRepository.save(product);
        return toResponse(product);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private void applyRequest(Product product, ProductRequest request) {
        if (request.getSku() != null) product.setSku(request.getSku());
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getDiscountPercent() != null) product.setDiscountPercent(request.getDiscountPercent());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());
        if (request.getLowStockThreshold() != null) product.setLowStockThreshold(request.getLowStockThreshold());
        if (request.getRating() != null) product.setRating(request.getRating());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
    }

    private Comparator<Product> sortComparator(String sort) {
        if (sort == null) return Comparator.comparing(Product::getId).reversed();
        return switch (sort) {
            case "priceAsc" -> Comparator.comparing(Product::getFinalPrice);
            case "priceDesc" -> Comparator.comparing(Product::getFinalPrice).reversed();
            case "nameAsc" -> Comparator.comparing(Product::getName);
            case "ratingDesc" -> Comparator.comparing(Product::getRating, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
            default -> Comparator.comparing(Product::getId).reversed();
        };
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .category(product.getCategory())
                .price(product.getPrice())
                .discountPercent(product.getDiscountPercent())
                .finalPrice(product.getFinalPrice())
                .quantity(product.getQuantity())
                .lowStockThreshold(product.getLowStockThreshold())
                .rating(product.getRating())
                .status(product.getStatus())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}