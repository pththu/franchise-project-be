package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.request.ManualAdjustPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.ManualAdjustPointsResponse;
import com.franchiseproject.loyaltyservice.service.PointAdjustmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loyalty")
@CrossOrigin(origins = "http://localhost:5173::")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PointAdjustmentController {

    PointAdjustmentService pointAdjustmentService;

    @PostMapping("/adjust-points")
    public ApiResponse<ManualAdjustPointsResponse> adjustPoints(
            @Valid @RequestBody ManualAdjustPointsRequest request) {

        return ApiResponse.<ManualAdjustPointsResponse>builder()
                .statusCode(200)
                .message("Points adjusted successfully")
                .data(pointAdjustmentService.manuallyAdjustPoints(request))
                .build();
    }
}