package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.request.ManualAdjustPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.ManualAdjustPointsResponse;

public interface PointAdjustmentService {
    ManualAdjustPointsResponse manuallyAdjustPoints(ManualAdjustPointsRequest request);
}