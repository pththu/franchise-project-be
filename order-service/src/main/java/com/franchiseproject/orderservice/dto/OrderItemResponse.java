package com.franchiseproject.orderservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
public class OrderItemResponse {
    private UUID productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
