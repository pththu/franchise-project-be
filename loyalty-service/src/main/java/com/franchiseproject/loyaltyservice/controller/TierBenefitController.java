package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.response.TierBenefitResponse;
import com.franchiseproject.loyaltyservice.service.TierBenefitService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TierBenefitController {

    TierBenefitService tierBenefitService;

    @GetMapping("/benefits")
    public ApiResponse<List<TierBenefitResponse>> getAllTierBenefits() {
        return ApiResponse.<List<TierBenefitResponse>>builder()
                .statusCode(200)
                .message("Get all tier benefits successfully")
                .data(tierBenefitService.getAllTierBenefits())
                .build();
    }
}