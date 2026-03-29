package com.franchiseproject.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentQRResponse {
    UUID orderId;
    UUID paymentTransactionId;
    String paymentUrl;
    String qrCodeUrl;
    Long amount;
    String method;
}
