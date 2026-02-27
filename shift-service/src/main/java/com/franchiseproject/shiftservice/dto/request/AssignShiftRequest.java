package com.franchiseproject.shiftservice.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AssignShiftRequest {

    private UUID staffId;
    private UUID shiftConfigId;
    private LocalDate workDate;
}