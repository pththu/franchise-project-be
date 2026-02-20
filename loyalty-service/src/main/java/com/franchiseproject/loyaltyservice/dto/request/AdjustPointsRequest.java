package com.franchiseproject.loyaltyservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdjustPointsRequest {
    @NotNull(message = "Customer ID is required")
    UUID customerId;

    @NotNull(message = "Points value is required")
    Integer points; // Truyền số âm nếu muốn trừ điểm, số dương nếu muốn cộng điểm
}