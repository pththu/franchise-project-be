package com.franchiseproject.orderservice.dto.response;

import com.franchiseproject.orderservice.enums.OrderStatus;
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
    OrderStatus orderStatus;
    String transactionReference;
    String message;
    BigDecimal finalTotal;
}
