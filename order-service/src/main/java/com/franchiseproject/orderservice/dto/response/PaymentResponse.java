package com.franchiseproject.orderservice.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    UUID paymentTransactionId;
    UUID orderId;
    UUID customerId;
    String orderStatus;
    String transactionReference;
    String message;
    BigDecimal finalTotal;
}
