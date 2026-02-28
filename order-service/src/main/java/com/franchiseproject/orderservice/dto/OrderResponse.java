package com.franchiseproject.orderservice.dto;

import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.enums.TypeOrder;
import com.franchiseproject.orderservice.model.OrderDetail;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    UUID id;
    UUID franchiseId;
    UUID customerId;
    UUID staffId;
    UUID paymentTransactionId;
    UUID promotionId;
    String address;
    BigDecimal totalDue;
    TypeOrder typeOrder;
    OrderStatus orderStatus;
    BigDecimal priceShip;
    Instant createAt;
    Instant updateAt;
    List<OrderItemResponse> orderDetails;
}
