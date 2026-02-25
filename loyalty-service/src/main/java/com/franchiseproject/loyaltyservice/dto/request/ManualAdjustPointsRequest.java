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
public class ManualAdjustPointsRequest {
    @NotNull(message = "CUSTOMER_ID_REQUIRED")
    UUID customerId;

    @NotNull(message = "FRANCHISE_ID_REQUIRED")
    UUID franchiseId;

    @NotNull(message = "POINTS_IS_REQUIRED")
    Integer points;

    @NotNull(message = "REASON_IS_REQUIRED")
    String reason;
}