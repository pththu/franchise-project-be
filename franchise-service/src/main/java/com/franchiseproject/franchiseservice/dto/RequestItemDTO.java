package com.franchiseproject.franchiseservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RequestItemDTO {
    private String productId;  // SỬA: Integer → String (vì product database dùng UUID)
    private String productCode;
    private String productName;
    private String size;
    private String color;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String imageUrl;
    private String category;
    private String productType;
    private String unit;
}