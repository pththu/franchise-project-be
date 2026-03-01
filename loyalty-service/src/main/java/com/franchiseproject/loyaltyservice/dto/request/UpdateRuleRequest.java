package com.franchiseproject.loyaltyservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateRuleRequest {
    @NotNull(message = "AMOUNT_PER_POINT_REQUIRED")
    Double amountPerPoint;
}