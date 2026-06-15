package com.franchiseproject.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrderItemRequest {

    @NotNull(message = "Không để trống id sản phẩm")
    UUID productId;

    UUID variantId;

    @Positive(message = "số lượng sản phẩm không âm")
    Integer quantity;
}
