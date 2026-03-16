package com.franchiseproject.paymentservice.dto.response.order;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    UUID id;
    UUID productId;
    String productNameSnapshot;
    Integer quantity;
    BigDecimal priceSnapshot;
    BigDecimal cost;
}
