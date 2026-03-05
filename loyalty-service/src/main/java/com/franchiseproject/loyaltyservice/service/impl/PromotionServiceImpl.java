package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.response.PromotionResponse;
import com.franchiseproject.loyaltyservice.repository.PromotionRepository;
import com.franchiseproject.loyaltyservice.service.PromotionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PromotionServiceImpl implements PromotionService {

    PromotionRepository promotionRepository;

    @Override
    public List<PromotionResponse> getAvailablePromotions() {
        LocalDateTime now = LocalDateTime.now();

        return promotionRepository.findAll().stream()
                .filter(p -> (p.getStartTime() == null || !now.isBefore(p.getStartTime())) &&
                        (p.getEndTime() == null || !now.isAfter(p.getEndTime())))
                .filter(p -> {
                    int usedCount = p.getCouponUsedCount() != null ? p.getCouponUsedCount() : 0;
                    int limit = p.getUsageLimit() != null ? p.getUsageLimit() : Integer.MAX_VALUE;
                    return usedCount < limit;
                })
                .map(p -> PromotionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .couponCode(p.getCouponCode())
                        .discountType(p.getDiscountType())
                        .discountValue(p.getDiscountValue())
                        .startTime(p.getStartTime())
                        .endTime(p.getEndTime())
                        .pointsToRedeem(p.getPointsToRedeem())
                        .build())
                .toList();
    }
}