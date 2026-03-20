package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.model.LoyaltyTier;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTierRepository;
import com.franchiseproject.loyaltyservice.service.LoyaltyTierService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoyaltyTierController {

    LoyaltyTierRepository tierBenefitRepository;

    @GetMapping("/tiers") // Thêm path rõ ràng
    public ApiResponse<List<LoyaltyTier>> getAllTiers() {
        List<LoyaltyTier> tiers = tierBenefitRepository.findAll();
        return ApiResponse.<List<com.franchiseproject.loyaltyservice.model.LoyaltyTier>>builder()
                .statusCode(200)
                .message("Get all tiers configuration successfully")
                .data(tiers)
                .build();
    }
}