package com.franchiseproject.orderservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDetailResponse {
    private UUID id;
    private String size;
    private String color;
    private BigDecimal price;
    private String imageUrl;
    private UUID productId;
    private String productName;
    private String brand;
    private String productType;
}
