package com.franchiseproject.orderservice.dto;

import com.franchiseproject.orderservice.enums.TypeOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        UUID franchiseId,
        UUID customerId,
        UUID staffId,
        UUID paymentTransactionId,
        UUID promotionId,
        @NotBlank(message = "Địa chỉ không được rỗng")
        String address,
        @Positive(message = "Giá không được âm")
        BigDecimal priceShip,
        TypeOrder typeOrder,
        List<CreateOrderItemRequest> items
) {
}
