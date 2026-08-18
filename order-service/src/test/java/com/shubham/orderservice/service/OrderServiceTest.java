package com.shubham.orderservice.service;

import com.shubham.orderservice.client.PaymentClient;
import com.shubham.orderservice.client.ProductClient;
import com.shubham.orderservice.dto.OrderStatusRequest;
import com.shubham.orderservice.entity.Order;
import com.shubham.orderservice.enums.OrderStatus;
import com.shubham.orderservice.exception.ForbiddenException;
import com.shubham.orderservice.exception.ResourceNotFoundException;
import com.shubham.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private ProductClient productClient;
    private PaymentClient paymentClient;
    private KafkaTemplate<String, Object> kafkaTemplate;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productClient = mock(ProductClient.class);
        paymentClient = mock(PaymentClient.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        orderService = new OrderService(orderRepository, productClient, paymentClient, kafkaTemplate);
    }

    private Order orderWithStatus(Long id, OrderStatus status) {
        return Order.builder()
                .id(id)
                .userId(10L)
                .productId(5L)
                .quantity(2)
                .totalPrice(new BigDecimal("2000.00"))
                .status(status)
                .build();
    }

    @Test
    void advanceStatus_validTransition_setsStatusAndTimestamp() {
        Order order = orderWithStatus(1L, OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderStatusRequest request = new OrderStatusRequest();
        request.setStatus("CONFIRMED");
        var response = orderService.advanceStatus(1L, request);

        assertEquals("CONFIRMED", response.getStatus());
        assertNotNull(order.getConfirmedAt());
        verify(orderRepository).save(order);
        verify(kafkaTemplate, atLeastOnce()).send(eq("order-events"), any(Map.class));
    }

    @Test
    void advanceStatus_invalidTransition_throwsIllegalArgument() {
        Order order = orderWithStatus(1L, OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderStatusRequest request = new OrderStatusRequest();
        request.setStatus("PAID");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.advanceStatus(1L, request));
        assertTrue(ex.getMessage().contains("Cannot change order"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void advanceStatus_invalidStatus_throwsIllegalArgument() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderWithStatus(1L, OrderStatus.UNPAID)));

        OrderStatusRequest request = new OrderStatusRequest();
        request.setStatus("GARBAGE");

        assertThrows(IllegalArgumentException.class, () -> orderService.advanceStatus(1L, request));
    }

    @Test
    void advanceStatus_orderNotFound_throwsNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        OrderStatusRequest request = new OrderStatusRequest();
        request.setStatus("CONFIRMED");

        assertThrows(ResourceNotFoundException.class, () -> orderService.advanceStatus(99L, request));
    }

    @Test
    void cancelOrder_unpaid_restoresStockAndNoRefund() {
        Order order = orderWithStatus(1L, OrderStatus.UNPAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.cancelOrder(1L);

        verify(productClient).restoreStock(eq(5L), eq(Map.of("quantity", 2)));
        verify(paymentClient, never()).refund(any(Map.class));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertNotNull(order.getCancelledAt());
    }

    @Test
    void cancelOrder_paid_alsoRefunds() {
        Order order = orderWithStatus(1L, OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentClient.refund(any(Map.class))).thenReturn(Map.of());

        orderService.cancelOrder(1L);

        verify(paymentClient).refund(Map.of("orderId", 1L));
        verify(productClient).restoreStock(eq(5L), eq(Map.of("quantity", 2)));
    }

    @Test
    void cancelOrder_delivered_throwsIllegalArgument() {
        Order order = orderWithStatus(1L, OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.cancelOrder(1L));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_ownerAccess_allowed() {
        Order order = orderWithStatus(1L, OrderStatus.UNPAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var response = orderService.getOrderById(1L, "10", "USER");

        assertEquals(1L, response.getId());
    }

    @Test
    void getOrderById_nonOwner_throwsForbidden() {
        Order order = orderWithStatus(1L, OrderStatus.UNPAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class, () -> orderService.getOrderById(1L, "99", "USER"));
    }

    @Test
    void getOrderById_adminBypassesOwnership() {
        Order order = orderWithStatus(1L, OrderStatus.UNPAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var response = orderService.getOrderById(1L, "99", "ADMIN");

        assertEquals(1L, response.getId());
    }

    @Test
    void getOrdersByUserId_ownerAccess_allowed() {
        when(orderRepository.findByUserId(10L)).thenReturn(List.of(orderWithStatus(1L, OrderStatus.UNPAID)));

        var responses = orderService.getOrdersByUserId(10L, "10", "USER");

        assertEquals(1, responses.size());
    }

    @Test
    void getOrdersByUserId_nonOwner_throwsForbidden() {
        assertThrows(ForbiddenException.class, () -> orderService.getOrdersByUserId(10L, "99", "USER"));
        verify(orderRepository, never()).findByUserId(any(Long.class));
    }

    @Test
    void getAnalytics_populatesMetrics() {
        when(orderRepository.count()).thenReturn(4L);
        when(orderRepository.sumRevenueAndPaidCount()).thenReturn(new Object[]{new BigDecimal("5000.00"), 3L});
        when(orderRepository.countCreatedSince(any())).thenReturn(2L);
        when(orderRepository.countByStatus()).thenReturn(List.<Object[]>of(new Object[]{OrderStatus.PAID, 3L}));
        when(orderRepository.sumQuantityByProduct()).thenReturn(List.<Object[]>of(new Object[]{5L, 2L}));
        when(orderRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of(orderWithStatus(1L, OrderStatus.PAID)));
        when(productClient.getProduct(5L)).thenReturn(Map.of("name", "Wireless Mouse"));

        var analytics = orderService.getAnalytics();

        assertEquals(4L, analytics.getTotalOrders());
        assertEquals(0, new BigDecimal("5000.00").compareTo(analytics.getTotalRevenue()));
        assertEquals(2L, analytics.getOrdersToday());
        assertEquals(1, analytics.getTopProducts().size());
        assertEquals("Wireless Mouse", analytics.getTopProducts().get(0).getProductName());
        assertEquals(3L, analytics.getOrderStatusCounts().get("PAID"));
        assertEquals(1, analytics.getRecentOrders().size());
    }
}