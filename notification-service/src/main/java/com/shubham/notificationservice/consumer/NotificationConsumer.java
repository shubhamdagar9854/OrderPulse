package com.shubham.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.notificationservice.entity.Notification;
import com.shubham.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(NotificationRepository notificationRepository, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void consumeOrderEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("Received order event: {}", event);

            String email = (String) event.getOrDefault("email", "user@example.com");
            Long orderId = event.get("orderId") != null
                    ? Long.valueOf(event.get("orderId").toString()) : null;
            String status = (String) event.getOrDefault("status", "UNKNOWN");

            String text = "Order #" + orderId + " is " + status + ". Thank you for your order!";
            log.info("Sending email to {}: {}", email, text);

            Notification notification = Notification.builder()
                    .orderId(orderId)
                    .email(email)
                    .message(text)
                    .build();
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to process order event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void consumePaymentEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("Received payment event: {}", event);

            String email = (String) event.getOrDefault("email", "user@example.com");
            Long orderId = event.get("orderId") != null
                    ? Long.valueOf(event.get("orderId").toString()) : null;
            String status = (String) event.getOrDefault("status", "UNKNOWN");

            String text = "Payment for Order #" + orderId + " is " + status + ".";
            log.info("Sending email to {}: {}", email, text);

            Notification notification = Notification.builder()
                    .orderId(orderId)
                    .email(email)
                    .message(text)
                    .build();
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", e.getMessage());
        }
    }
}
