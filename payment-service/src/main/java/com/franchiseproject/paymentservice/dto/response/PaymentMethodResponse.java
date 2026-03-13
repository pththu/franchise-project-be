package com.franchiseproject.paymentservice.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentMethodResponse {
    UUID id;
    String methodName;
    String provider;
    boolean active;
}
