package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.request.ManageTierBenefitRequest;
import com.franchiseproject.loyaltyservice.dto.response.TierBenefitResponse;
import com.franchiseproject.loyaltyservice.service.TierBenefitService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty")
@CrossOrigin(origins = "http://localhost:5173")
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
}