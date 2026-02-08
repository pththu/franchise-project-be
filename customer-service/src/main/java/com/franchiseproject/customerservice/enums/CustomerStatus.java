package com.franchiseproject.customerservice.enums;

public enum CustomerStatus {
    ACTIVE,        // Khách hàng đang hoạt động bình thường
    INACTIVE,      // Chưa hoạt động / lâu không giao dịch
    SUSPENDED,     // Bị khóa tạm thời (vi phạm, gian lận)
    DELETED
}
