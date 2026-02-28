package com.franchiseproject.orderservice.dto.response;

import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.enums.TypeOrder;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderByCustomerResponse {
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
    List<OrderDetailResponse> orderDetails;
}
