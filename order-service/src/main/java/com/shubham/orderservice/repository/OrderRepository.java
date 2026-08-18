package com.shubham.orderservice.repository;

import com.shubham.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @Query("select sum(o.totalPrice), count(o) from Order o " +
            "where o.status in (com.shubham.orderservice.enums.OrderStatus.PAID, " +
            "com.shubham.orderservice.enums.OrderStatus.CONFIRMED, " +
            "com.shubham.orderservice.enums.OrderStatus.PROCESSING, " +
            "com.shubham.orderservice.enums.OrderStatus.SHIPPED, " +
            "com.shubham.orderservice.enums.OrderStatus.DELIVERED)")
    Object[] sumRevenueAndPaidCount();

    @Query("select count(o) from Order o where o.createdAt >= :start")
    long countCreatedSince(@Param("start") LocalDateTime start);

    @Query("select o.status, count(o) from Order o group by o.status")
    List<Object[]> countByStatus();

    @Query("select o.productId, sum(o.quantity) from Order o group by o.productId order by sum(o.quantity) desc")
    List<Object[]> sumQuantityByProduct();

    List<Order> findTop5ByOrderByCreatedAtDesc();
}