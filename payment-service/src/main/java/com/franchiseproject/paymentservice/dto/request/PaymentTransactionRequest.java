package com.franchiseproject.paymentservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class PaymentTransactionRequest {
    @NotNull(message = "orderId không được trống")
    UUID orderId;
    @NotNull(message = "customerId không được trống")
    UUID customerId;
    @Positive(message = "Tiền > 0")
    BigDecimal finalTotal;
}
