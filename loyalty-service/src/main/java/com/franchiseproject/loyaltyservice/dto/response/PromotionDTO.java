package com.franchiseproject.loyaltyservice.dto.response;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class PromotionDTO {
    private UUID id;
    private String name;
    private String status;
    private Integer requiredPoints;
    private Instant startTime;
    private Instant endTime;
}