package com.franchiseproject.orderservice.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultRequest {
    UUID orderId;
    UUID paymentTransactionId;
    String transactionRef;
    BigDecimal amount;
    String paymentMethod;
    String status;
}
