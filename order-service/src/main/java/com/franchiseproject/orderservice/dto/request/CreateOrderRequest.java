package com.franchiseproject.orderservice.dto.request;

import com.franchiseproject.orderservice.enums.TypeOrder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrderRequest {
    @NotNull(message = "Không để trống franchise")
    UUID franchiseId;
    UUID customerId;
    UUID staffId;
    UUID promotionId;
    String address;
    @PositiveOrZero(message = "Chỉ được nhập >= 0")
    BigDecimal priceShip;
    @NotNull(message = "Không để trống")
    TypeOrder typeOrder;
    @NotNull(message = "Order phải có sản phẩm")
    List<CreateOrderItemRequest> items;
}
