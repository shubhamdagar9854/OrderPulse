package com.shubham.paymentservice.repository;

import com.shubham.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderIdAndStatus(Long orderId, String status);

    Optional<Payment> findTopByOrderIdAndStatusOrderByUpdatedAtDesc(Long orderId, String status);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}