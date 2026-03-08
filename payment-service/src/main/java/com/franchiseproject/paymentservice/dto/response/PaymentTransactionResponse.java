package com.franchiseproject.paymentservice.dto.response;


import com.franchiseproject.paymentservice.enums.StatusTransaction;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransactionResponse {
    UUID id;
    UUID userId;
    BigDecimal amount;
    StatusTransaction status;
    String transactionRef;
    LocalDateTime createdAt;
    String paymentMethodName;
}
