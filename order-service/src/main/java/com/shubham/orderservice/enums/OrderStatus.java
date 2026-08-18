package com.shubham.orderservice.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum OrderStatus {

    UNPAID, PAID, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED;

    private static final EnumMap<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);
    private static final List<OrderStatus> TIMELINE = List.of(UNPAID, PAID, CONFIRMED, PROCESSING, SHIPPED, DELIVERED);
    private static final Set<OrderStatus> CANCELLABLE = EnumSet.of(UNPAID, PAID, CONFIRMED, PROCESSING);

    static {
        ALLOWED_TRANSITIONS.put(UNPAID, EnumSet.of(PAID, CANCELLED));
        ALLOWED_TRANSITIONS.put(PAID, EnumSet.of(CONFIRMED, CANCELLED));
        ALLOWED_TRANSITIONS.put(CONFIRMED, EnumSet.of(PROCESSING, CANCELLED));
        ALLOWED_TRANSITIONS.put(PROCESSING, EnumSet.of(SHIPPED, CANCELLED));
        ALLOWED_TRANSITIONS.put(SHIPPED, EnumSet.of(DELIVERED));
        ALLOWED_TRANSITIONS.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }

    public boolean isCancellable() {
        return CANCELLABLE.contains(this);
    }

    public boolean isTerminal() {
        return ALLOWED_TRANSITIONS.get(this).isEmpty();
    }

    public int timelinePosition() {
        return TIMELINE.indexOf(this);
    }

    public String label() {
        return switch (this) {
            case UNPAID -> "Pending Payment";
            case PAID -> "Payment Received";
            case CONFIRMED -> "Order Confirmed";
            case PROCESSING -> "Processing";
            case SHIPPED -> "Shipped";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
        };
    }

    public static List<OrderStatus> timeline() {
        return TIMELINE;
    }

    public static boolean isValid(String value) {
        return Arrays.stream(values()).anyMatch(s -> s.name().equalsIgnoreCase(value));
    }
}