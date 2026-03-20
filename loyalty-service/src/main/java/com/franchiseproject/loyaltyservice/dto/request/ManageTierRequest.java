package com.franchiseproject.loyaltyservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ManageTierRequest {

    @NotNull(message = "TIER_NAME_REQUIRED")
    String tierName;

    @NotNull(message = "REQUIRED_POINTS_REQUIRED")
    Integer requiredPoints;

}