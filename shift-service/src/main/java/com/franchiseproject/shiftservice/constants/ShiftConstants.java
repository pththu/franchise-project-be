package com.franchiseproject.shiftservice.constants;

public interface ShiftConstants {
    int GRACE_PERIOD_MINUTES = 15;      // Thời gian ân hạn check-in
    int LATE_THRESHOLD_MINUTES = 30;    // Check-in trễ sau bao nhiêu phút

    // Thông báo lỗi
    String ERROR_SHIFT_NOT_FOUND = "Không tìm thấy ca làm việc";
    String ERROR_STAFF_SHIFT_NOT_FOUND = "Không tìm thấy ca đã phân công";
    String ERROR_SHIFT_INACTIVE = "Ca làm việc đã bị vô hiệu hóa";
    String ERROR_PAST_SHIFT = "Không thể phân ca trong quá khứ";
    String ERROR_DUPLICATE_SHIFT = "Nhân viên đã có ca làm việc trong ngày này";
    String ERROR_ALREADY_CHECKED_IN = "Nhân viên đã check-in rồi";
    String ERROR_NOT_CHECKED_IN = "Chưa check-in không thể check-out";
    String ERROR_INVALID_STATUS = "Trạng thái không hợp lệ";
}