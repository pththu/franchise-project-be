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
    @NotNull(message = "INVALID_KEY")
    UUID customerId;

    @NotNull(message = "INVALID_KEY")
    UUID franchiseId;

    @NotNull(message = "INVALID_KEY")
    Integer points;

    String reason;
}