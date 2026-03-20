package com.franchiseproject.franchiseservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RequestItemDTO {
    private Integer productId;
    private String productCode;
    private String productName;
    private String size;
    private String color;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice; // price * quantity
    private String imageUrl;
    private String category;
}