package com.franchiseproject.shiftservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftStatisticResponse {

    private long totalShifts;
    private long totalAssigned;
    private long totalCheckedIn;
    private long totalCheckedOut;
    private long totalAbsent;
}
