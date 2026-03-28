package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerLoyaltyResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerTierResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import com.franchiseproject.loyaltyservice.service.CustomerTierService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loyalty")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerTierController {

<<<<<<< HEAD
    CustomerTierService customerTierService;

    @GetMapping("/customers/{customerId}/franchises/{franchiseId}/tier-info")
    public ApiResponse<CustomerTierResponse> getCustomerTierInfo(
            @PathVariable UUID customerId,
            @PathVariable UUID franchiseId) {

        return ApiResponse.<CustomerTierResponse>builder()
                .statusCode(200)
                .message("Get customer tier info successfully")
                .data(customerTierService.getCustomerTierInfo(customerId, franchiseId))
                .build();
    }
    @GetMapping("/customers/phone/{phone}/franchises/{franchiseId}/tier-info")
    public ApiResponse<CustomerTierResponse> getCustomerTierInfoByPhone(
            @PathVariable String phone,
            @PathVariable UUID franchiseId) {

        return ApiResponse.<CustomerTierResponse>builder()
                .statusCode(200)
                .message("Get customer tier info by phone successfully")
                .data(customerTierService.getCustomerTierInfoByPhone(phone, franchiseId))
                .build();
    }
=======
    private final CustomerTierService customerTierService;
>>>>>>> sprint04

    @GetMapping("/customers")
    public ApiResponse<List<CustomerLoyaltyResponse>> getCustomersByTier(
            @RequestParam(value = "tier", required = false) CustomerLoyaltyTier tier) {
        return ApiResponse.<List<CustomerLoyaltyResponse>>builder()
                .statusCode(200)
                .message("Get customers by tier successfully")
                .data(customerTierService.getCustomersByTier(tier))
                .build();
    }

    @PostMapping("/internal/customers/bulk-tier-info")
    public ApiResponse<List<CustomerTierResponse>> getBulkCustomerTierInfo(@RequestBody List<UUID> customerIds) {
        return ApiResponse.<List<CustomerTierResponse>>builder()
                .statusCode(200)
                .message("Bulk fetch loyalty tier info success")
                .data(customerTierService.getBulkCustomerTierInfo(customerIds))
                .build();
    }
}