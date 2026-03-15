package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.request.ManageTierBenefitRequest;
import com.franchiseproject.loyaltyservice.dto.response.TierBenefitResponse;
import com.franchiseproject.loyaltyservice.model.TierBenefit;
import com.franchiseproject.loyaltyservice.repository.TierBenefitRepository;
import com.franchiseproject.loyaltyservice.service.TierBenefitService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TierBenefitController {

    TierBenefitService tierBenefitService;
    TierBenefitRepository tierBenefitRepository;

    @GetMapping("/benefits")
    public ApiResponse<List<TierBenefitResponse>> getAllTierBenefits() {
        return ApiResponse.<List<TierBenefitResponse>>builder()
                .statusCode(200)
                .message("Get all tier benefits successfully")
                .data(tierBenefitService.getAllTierBenefits())
                .build();
    }

    @PostMapping("/benefits")
    public ApiResponse<String> manageTierBenefits(@Valid @RequestBody ManageTierBenefitRequest request) {

        tierBenefitService.manageTierBenefits(request);

        return ApiResponse.<String>builder()
                .statusCode(200)
                .message("Tier configuration saved successfully")
                .data("OK")
                .build();
    }

    @DeleteMapping("/benefits/{tierName}")
    public ApiResponse<String> deleteTierBenefit(@PathVariable String tierName) {
        tierBenefitService.deleteTierBenefit(tierName);
        return ApiResponse.<String>builder()
                .statusCode(200)
                .message("Tier deleted successfully")
                .data("OK")
                .build();
    }

    @GetMapping("/tiers") // Thêm path rõ ràng
    public ApiResponse<List<TierBenefit>> getAllTiers() {
        List<TierBenefit> tiers = tierBenefitRepository.findAll();
        return ApiResponse.<List<TierBenefit>>builder()
                .statusCode(200)
                .message("Get all tiers configuration successfully")
                .data(tiers)
                .build();
    }
}