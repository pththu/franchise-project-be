package com.franchiseproject.shiftservice.dto;

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
}