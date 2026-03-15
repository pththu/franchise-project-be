package com.franchiseproject.shiftservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonalStatisticResponse {

    private long totalShifts;
    private long totalCompleted;
    private long totalAbsent;
    private long totalLate;           // Số lần check-in trễ
    private long totalIncomplete;     // Số lần quên check-out
}