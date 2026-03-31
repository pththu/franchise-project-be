package com.franchiseproject.orderservice.enums;

public enum OrderStatus {
    WAITING_FOR_CONFIRMATION,  //Chờ xác nhận
    PREPARING,  //Đã xác nhận, đang chuẩn bị
    SHIPPING,
    COMPLETED,
    CANCELLED,
    FAILED_ORDER,
    REFUNDED;

    public boolean canBeCancelledByCustomer() {
        return this == WAITING_FOR_CONFIRMATION ||
                this == PREPARING;
    }
}
