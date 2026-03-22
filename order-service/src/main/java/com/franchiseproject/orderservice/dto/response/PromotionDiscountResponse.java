package com.franchiseproject.orderservice.dto.response;

import com.franchiseproject.orderservice.enums.DiscountType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionDiscountResponse {
    UUID promotionUsageId;
    BigDecimal discountValue;
    DiscountType discountType;
}
