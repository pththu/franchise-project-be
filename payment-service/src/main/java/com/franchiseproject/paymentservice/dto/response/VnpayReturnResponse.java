package com.franchiseproject.paymentservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VnpayReturnResponse {
    String txnRef;
    String amount;
    String responseCode;
}
