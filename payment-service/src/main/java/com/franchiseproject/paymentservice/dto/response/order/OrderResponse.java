package com.franchiseproject.paymentservice.dto.response.order;

import com.franchiseproject.paymentservice.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    UUID id;
    UUID franchiseId;
    UUID customerId;
    UUID staffId;
    UUID paymentTransactionId;
    UUID promotionId;
    String transactionReference;
    String address;
    BigDecimal totalDue;
    String typeOrder;
    String orderStatus;
    BigDecimal priceShip;
    Instant createAt;
    Instant updateAt;
    List<OrderItemResponse> orderDetails;
}
