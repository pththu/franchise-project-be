package com.franchiseproject.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EarnPointsRequest {
    @NotNull
    UUID userId;

    @NotNull
    UUID orderId;

    UUID promotionId;

    @NotNull
    UUID franchiseId;

    @NotNull
    Double orderAmount;
}
