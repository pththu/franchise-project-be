package com.franchiseproject.orderservice.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductResponse {
    UUID id;
    String name;
    BigDecimal price;
    Integer quantity;
}
