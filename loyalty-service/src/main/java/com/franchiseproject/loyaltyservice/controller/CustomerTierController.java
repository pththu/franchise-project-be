package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerLoyaltyResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import com.franchiseproject.loyaltyservice.service.CustomerTierService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerTierController {

    private final CustomerTierService customerTierService;

    @GetMapping("/customers")
    public ApiResponse<List<CustomerLoyaltyResponse>> getCustomersByTier(
            @RequestParam(value = "tier", required = false) CustomerLoyaltyTier tier) {

        return ApiResponse.<List<CustomerLoyaltyResponse>>builder()
                .statusCode(200)
                .message("Get customers by tier successfully")
                .data(customerTierService.getCustomersByTier(tier))
                .build();
    }
}