package com.franchiseproject.orderservice.enums;

public enum OrderStatus {
    CREATED,
    WAITING_PAYMENT,
    PAID,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED,
    FAILED_ORDER,
    FAILED_PAYMENT,
    REFUNDED;

    public boolean canBeCancelledByCustomer() {
        return this == CREATED ||
                this == WAITING_PAYMENT ||
                this == PAID;
    }
}
