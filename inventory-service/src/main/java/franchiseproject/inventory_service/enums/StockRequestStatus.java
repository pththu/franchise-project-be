package franchiseproject.inventory_service.enums;

public enum StockRequestStatus {
    PENDING,    // Đang chờ duyệt
    APPROVED,   // Đã phê duyệt
    REJECTED,   // Từ chối
    SHIPPED,    // Đang giao hàng
    RECEIVED,   // Đã nhận hàng
    CANCELLED   // Đã hủy
}
