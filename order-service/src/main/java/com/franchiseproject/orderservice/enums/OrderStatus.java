package com.franchiseproject.orderservice.enums;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    WAITING_PAYMENT,
    PAID,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED,
    FAILED,
    REFUNDED;

    public boolean canBeCancelledByCustomer() {
        return this == CREATED ||
                this == WAITING_PAYMENT ||
                this == CONFIRMED ||
                this == PAID;
    }
}
