package com.franchiseproject.orderservice.dto;

import com.franchiseproject.orderservice.enums.TypeOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        UUID franchiseId,
        UUID customerId,
        UUID staffId,
        UUID paymentTransactionId,
        UUID promotionId,
        String address,
        BigDecimal priceShip,
        TypeOrder typeOrder,
        List<CreateOrderItemRequest> items
) {
}
