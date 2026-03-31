package com.franchiseproject.orderservice.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePaymentEvent {
    UUID orderId;
    UUID paymentMethodId;
}
