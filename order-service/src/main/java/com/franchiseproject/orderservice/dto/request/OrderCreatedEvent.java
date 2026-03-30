package com.franchiseproject.orderservice.dto.request;

import java.util.UUID;

public class OrderCreatedEvent {
    private UUID orderId;
    private UUID paymentMethodId;
}
