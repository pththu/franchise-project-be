package com.franchiseproject.orderservice.enums;

public enum OrderStatus {
    CREATED,      // Đơn vừa được tạo (chưa thanh toán)
    CONFIRMED,     // Đơn hợp lệ, đã xác nhận (thường sau khi giữ hàng / xác thực)
    WAITING_PAYMENT,// Chờ thanh toán (online)
    PAID,    // Đã thanh toán thành công
    PREPARING,     // Đang chuẩn bị hàng
    READY,   // Sẵn sàng giao / lấy tại quầy
    COMPLETED,      // Hoàn tất (khách nhận hàng)
    CANCELED,  // Đơn bị hủy (trước khi giao)
    FAILED,       // Đơn lỗi (payment fail, timeout, hệ thống)
    REFUNDED, // Đã hoàn tiền
}
