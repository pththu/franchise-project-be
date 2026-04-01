package com.franchiseproject.orderservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EarnPointsRequest {
    UUID userId;
    UUID orderId;
    UUID promotionId;
    UUID franchiseId;
    Double orderAmount;
}
