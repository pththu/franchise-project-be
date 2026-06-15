package com.franchiseproject.orderservice.dto.request;

import com.franchiseproject.orderservice.enums.TypeOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdateOrderRequest {
    UUID orderId;
    UUID staffId;
    UUID paymentTransactionId;
    UUID promotionId;
    String address;
    private BigDecimal priceShip;
    TypeOrder typeOrder;
    List<UpdateOrderItemRequest> items;
}
