package com.franchiseproject.orderservice.dto;

import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.enums.TypeOrder;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class OrderResponse {
    private UUID orderId;
    private UUID franchiseId;
    private UUID customerId;
    private UUID staffId;
    private BigDecimal totalDue;
    private BigDecimal priceShip;
    private OrderStatus orderStatus;
    private TypeOrder typeOrder;
    private Instant createdAt;
    private List<OrderItemResponse> items;
}
