package com.franchiseproject.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyReserveRequest {
    @NotNull(message = "customerId không được trống")
    UUID customerId;
    @PositiveOrZero(message = "Điểm không được âm")
    Integer point;//điểm của khách hàng để trừ vào order
}
