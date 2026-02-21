package com.franchiseproject.loyaltyservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdjustPointsRequest {
    @NotNull(message = "Customer ID is required")
    UUID customerId;

    @NotNull(message = "Points value is required")
    Integer points; // Truyền số âm nếu muốn trừ điểm, số dương nếu muốn cộng điểm
}