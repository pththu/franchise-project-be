package com.franchiseproject.shiftservice.dto.response;

import com.franchiseproject.shiftservice.enums.ShiftStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class StaffShiftResponse {
    private UUID id;
    private UUID staffId;
    private UUID shiftConfigId;
    private LocalDate workDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private ShiftStatus status;
    private Integer lateMinutes;      // Số phút check-in trễ
    private String note;              // Ghi chú
    private String shiftName;         // Tên ca
    private LocalTime shiftStartTime; // Giờ bắt đầu ca
    private LocalTime shiftEndTime;   // Giờ kết thúc ca
}