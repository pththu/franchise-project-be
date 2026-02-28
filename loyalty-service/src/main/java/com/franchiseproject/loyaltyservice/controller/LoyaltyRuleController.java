package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.request.UpdateRuleRequest;
import com.franchiseproject.loyaltyservice.service.impl.LoyaltyRuleServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/loyalty/rules")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoyaltyRuleController {

    LoyaltyRuleServiceImpl loyaltyRuleService;

    @GetMapping
    public ApiResponse<Double> getRule() {
        return ApiResponse.<Double>builder()
                .statusCode(200)
                .message("Success")
                .data(loyaltyRuleService.getAmountPerPoint())
                .build();
    }

    @PostMapping
    public ApiResponse<String> updateRule(@Valid @RequestBody UpdateRuleRequest request) {
        loyaltyRuleService.updateRule(request);
        return ApiResponse.<String>builder()
                .statusCode(200)
                .message("Rule updated successfully")
                .data("OK")
                .build();
    }
}