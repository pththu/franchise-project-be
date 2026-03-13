package com.franchiseproject.paymentservice.dto.response;

import com.franchiseproject.paymentservice.entity.PaymentMethod;

import com.franchiseproject.paymentservice.enums.StatusTransaction;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransactionResponse {
    UUID id;
    UUID userId;
    UUID orderId;
    BigDecimal amount;
    StatusTransaction status;
    String transactionRef;
    LocalDateTime createdAt;
    PaymentMethodResponse paymentMethodResponse;
    String paymentMethodName;
}
