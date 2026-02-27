package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.response.PromotionResponse;
import java.util.List;

public interface PromotionService {
    List<PromotionResponse> getAvailablePromotions();
}