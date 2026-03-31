package com.franchiseproject.customerservice.dto.response;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerSummaryResponse {
    UserResponse user; // Thông tin định danh từ Identity
    CustomerTierResponse loyaltyInfo; // Thông tin hạng thẻ từ Loyalty
    List<CustomerFranchiseSummary> purchasedFranchises; // Danh sách chi nhánh + order metadata
}
