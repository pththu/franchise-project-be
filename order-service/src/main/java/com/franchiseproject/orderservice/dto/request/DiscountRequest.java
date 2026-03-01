package com.franchiseproject.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiscountRequest {
    @NotNull(message = "customerId không được trống")
    UUID customerId;
    @NotNull(message = "promtionId không được trống")
    UUID promotionId;
    @PositiveOrZero(message = "Tiền không được âm")
    BigDecimal totalItems;
}
