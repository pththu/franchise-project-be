package com.franchiseproject.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderItemRequest(
        UUID productId,
        String productName,
        BigDecimal price,
        BigDecimal cost,
        Integer quantity
) {}
