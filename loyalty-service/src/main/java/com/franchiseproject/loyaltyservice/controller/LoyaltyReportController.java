package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.response.LoyaltyReportResponse;
import com.franchiseproject.loyaltyservice.service.LoyaltyReportService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loyalty/loyalty-reports")
@CrossOrigin(origins = "http://localhost:5173")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoyaltyReportController {

    LoyaltyReportService loyaltyReportService;

    // View Loyalty program reports
    @GetMapping
    public ApiResponse<LoyaltyReportResponse> getLoyaltyReport() {
        return ApiResponse.<LoyaltyReportResponse>builder()
                .statusCode(200)
                .message("Get loyalty report successfully")
                .data(loyaltyReportService.getLoyaltyReport())
                .build();
    }
}