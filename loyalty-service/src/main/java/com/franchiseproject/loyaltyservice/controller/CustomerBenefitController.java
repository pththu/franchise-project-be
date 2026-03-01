package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerBenefitResponse;
import com.franchiseproject.loyaltyservice.service.CustomerBenefitService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loyalty")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerBenefitController {

    CustomerBenefitService customerBenefitService;

    @GetMapping("/customers/{customerId}/franchises/{franchiseId}/benefits")
    public ApiResponse<CustomerBenefitResponse> getMyBenefits(
            @PathVariable UUID customerId,
            @PathVariable UUID franchiseId) {

        return ApiResponse.<CustomerBenefitResponse>builder()
                .statusCode(200)
                .message("Get customer tier benefits successfully")
                .data(customerBenefitService.getCustomerBenefits(customerId, franchiseId))
                .build();
    }
}