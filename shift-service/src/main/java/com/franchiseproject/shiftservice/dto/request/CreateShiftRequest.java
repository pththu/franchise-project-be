package com.franchiseproject.shiftservice.dto.request;

import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateShiftRequest {
    private UUID franchiseId;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
}