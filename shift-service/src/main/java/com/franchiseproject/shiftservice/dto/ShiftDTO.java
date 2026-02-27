package com.franchiseproject.shiftservice.dto;

import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
public class ShiftDTO {
    private UUID id;
    private UUID franchiseId;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
}