package com.shubham.orderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.orderservice.client.ProductClient;
import com.shubham.orderservice.dto.CartItemRequest;
import com.shubham.orderservice.dto.CartItemResponse;
import com.shubham.orderservice.dto.OrderRequest;
import com.shubham.orderservice.dto.OrderResponse;
import com.shubham.orderservice.entity.CartItem;
import com.shubham.orderservice.exception.ForbiddenException;
import com.shubham.orderservice.exception.ResourceNotFoundException;
import com.shubham.orderservice.exception.ServiceUnavailableException;
import com.shubham.orderservice.repository.CartItemRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;
    private final OrderService orderService;

    public CartService(CartItemRepository cartItemRepository, ProductClient productClient, OrderService orderService) {
        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public CartItemResponse getCart(Long userId, String xUserId, String xRole) {
        assertOwner(userId, xUserId, xRole);
        return buildResponse(userId);
    }

    @Transactional
    public CartItemResponse addItem(Long userId, CartItemRequest request, String xUserId, String xRole) {
        assertOwner(userId, xUserId, xRole);
        Map<String, Object> product = fetchProduct(request.getProductId());
        int available = Integer.parseInt(product.get("quantity").toString());
        if (request.getQuantity() > available) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + available);
        }

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElse(new CartItem(userId, request.getProductId(), 0));
        item.setQuantity(item.getQuantity() + request.getQuantity());
        item.touch();
        cartItemRepository.save(item);
        return buildResponse(userId);
    }

    @Transactional
    public CartItemResponse updateQuantity(Long userId, Long productId, CartItemRequest request, String xUserId, String xRole) {
        assertOwner(userId, xUserId, xRole);
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " not in cart"));

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            cartItemRepository.delete(item);
        } else {
            Map<String, Object> product = fetchProduct(productId);
            int available = Integer.parseInt(product.get("quantity").toString());
            if (request.getQuantity() > available) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + available);
            }
            item.setQuantity(request.getQuantity());
            item.touch();
            cartItemRepository.save(item);
        }
        return buildResponse(userId);
    }

    @Transactional
    public CartItemResponse removeItem(Long userId, Long productId, String xUserId, String xRole) {
        assertOwner(userId, xUserId, xRole);
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " not in cart"));
        cartItemRepository.delete(item);
        return buildResponse(userId);
    }

    @Transactional
    public CartItemResponse clear(Long userId, String xUserId, String xRole) {
        assertOwner(userId, xUserId, xRole);
        cartItemRepository.deleteByUserId(userId);
        return buildResponse(userId);
    }

    @Transactional
    public List<OrderResponse> checkout(Long userId, String xUserId, String xRole) {
        assertOwner(userId, xUserId, xRole);
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Add products before checkout.");
        }

        List<OrderResponse> orders = new ArrayList<>();
        for (CartItem item : items) {
            try {
                OrderResponse order = orderService.createOrder(
                        new OrderRequest(userId, item.getProductId(), item.getQuantity()));
                orders.add(order);
                log.info("Checkout order created: {} for user {}", order.getId(), userId);
            } catch (FeignException e) {
                throw new ServiceUnavailableException("Checkout failed, product service unavailable. Please try again.");
            }
        }
        cartItemRepository.deleteByUserId(userId);
        return orders;
    }

    private Map<String, Object> fetchProduct(Long productId) {
        try {
            return productClient.getProduct(productId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Product service unavailable. Please try again.");
        }
    }

    private CartItemResponse buildResponse(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        CartItemResponse response = new CartItemResponse();
        response.setUserId(userId);
        List<CartItemResponse.Item> detailItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        for (CartItem item : items) {
            Map<String, Object> product;
            try {
                product = productClient.getProduct(item.getProductId());
            } catch (FeignException e) {
                log.warn("Product {} unavailable, skipping in cart", item.getProductId());
                continue;
            }
            BigDecimal price = new BigDecimal(product.get("finalPrice").toString());
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);
            count += item.getQuantity();

            CartItemResponse.Item detail = new CartItemResponse.Item();
            detail.setProductId(item.getProductId());
            detail.setProductName(product.get("name") != null ? product.get("name").toString() : "Product " + item.getProductId());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(price);
            detail.setLineTotal(lineTotal);
            detailItems.add(detail);
        }

        response.setItems(detailItems);
        response.setItemCount(count);
        response.setTotalAmount(total);
        return response;
    }

    private void assertOwner(Long userId, String xUserId, String xRole) {
        if ("ADMIN".equals(xRole)) return;
        if (xUserId != null && !xUserId.isBlank() && String.valueOf(userId).equals(xUserId)) return;
        throw new ForbiddenException("You do not have permission to access this cart");
    }
}