package com.example.reportservice.dto.order;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemResponse {
    private UUID id;
    private UUID productId;
    private String productNameSnapshot;
    private Integer quantity;
    private BigDecimal priceSnapshot;
    private BigDecimal cost;
}