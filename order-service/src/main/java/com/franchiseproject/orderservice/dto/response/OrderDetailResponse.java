package com.franchiseproject.orderservice.dto.response;



import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    UUID id;
    UUID productId;
    String productNameSnapshot;
    Integer quantity;
    BigDecimal priceSnapshot;
    BigDecimal cost;
}
