package com.franchiseproject.orderservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PosCartItem {
    UUID productId;
    Integer quantity;
}
