package com.example.reportservice.dto.product;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductResponse {
    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
}