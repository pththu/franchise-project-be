package com.franchiseproject.shiftservice.dto.response;

import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
public class ShiftResponse {
    private UUID id;
    private UUID franchiseId;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
    private Boolean status;
}
