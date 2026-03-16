package com.franchiseproject.orderservice.enums;

public enum OrderStatus {
    CREATED, /// khi method build order từ data request gửi về
    CONFIRMED,
    WAITING_PAYMENT,/// khi method createOrder trả về orderId cho client
    PAID, /// Khi payment-service trả kết quả thanh toán (SUCCESS) cho order-service
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED, ///User hủy
    FAILED,/// Khi payment-service trả kết quả thanh toán (Failed) cho order-service
    REFUNDED;

    public boolean canBeCancelledByCustomer() {
        return this == CREATED ||
                this == WAITING_PAYMENT ||
                this == CONFIRMED ||
                this == PAID;
    }
}
