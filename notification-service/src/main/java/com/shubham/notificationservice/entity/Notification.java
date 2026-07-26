package com.shubham.notificationservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private String email;
    private String message;

    private LocalDateTime sentAt = LocalDateTime.now();

    public Notification() {}
    public Notification(Long id, Long orderId, String email, String message, LocalDateTime sentAt) {
        this.id = id; this.orderId = orderId; this.email = email; this.message = message; this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public static NotificationBuilder builder() { return new NotificationBuilder(); }

    public static class NotificationBuilder {
        private Long id; private Long orderId; private String email; private String message; private LocalDateTime sentAt = LocalDateTime.now();
        NotificationBuilder() {}
        public NotificationBuilder id(Long id) { this.id = id; return this; }
        public NotificationBuilder orderId(Long orderId) { this.orderId = orderId; return this; }
        public NotificationBuilder email(String email) { this.email = email; return this; }
        public NotificationBuilder message(String message) { this.message = message; return this; }
        public NotificationBuilder sentAt(LocalDateTime sentAt) { this.sentAt = sentAt; return this; }
        public Notification build() { return new Notification(id, orderId, email, message, sentAt); }
    }
}
