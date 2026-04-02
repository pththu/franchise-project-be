package com.franchiseproject.orderservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    UUID productId;
    UUID variantId;
    int quantity;
    String productName;
    String size;
    String color;
    BigDecimal price;
    String imageUrl;
    int stock;
}
