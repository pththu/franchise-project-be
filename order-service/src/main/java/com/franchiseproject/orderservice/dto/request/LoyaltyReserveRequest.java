package com.franchiseproject.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyReserveRequest {
    @NotNull(message = "userId không được trống")
    UUID userId;

    @NotNull(message = "franchiseId không được trống")
    UUID franchiseId;

    @NotNull(message = "orderId không được trống")
    UUID orderId;

    @PositiveOrZero(message = "Điểm không được âm")
    Integer pointsToDeduct;
}
