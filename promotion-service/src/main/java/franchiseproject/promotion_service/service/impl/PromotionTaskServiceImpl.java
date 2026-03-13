package franchiseproject.promotion_service.service.impl;

import franchiseproject.promotion_service.dto.PromotionEffectiveTimeRequest;
import franchiseproject.promotion_service.dto.PromotionUsageCheckResponse;
import franchiseproject.promotion_service.dto.UpdatePromotionUsageRequest;
import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.exception.ResourceNotFoundException;
import franchiseproject.promotion_service.repository.PromotionRepository;
import franchiseproject.promotion_service.service.PromotionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionTaskServiceImpl implements PromotionTaskService {

    private final PromotionRepository promotionRepository;

    @Override
    public Promotion definePromotionEffectiveTime(UUID promotionId, PromotionEffectiveTimeRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        validateEffectiveTime(request);

        if (promotion.getStartTime() != null || promotion.getEndTime() != null) {
            throw new IllegalArgumentException("Promotion effective time already defined. Use update API instead.");
        }

        promotion.setStartTime(request.getStartTime());
        promotion.setEndTime(request.getEndTime());

        return promotionRepository.save(promotion);
    }

    @Override
    public Promotion updatePromotionEffectiveTime(UUID promotionId, PromotionEffectiveTimeRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        validateEffectiveTime(request);

        promotion.setStartTime(request.getStartTime());
        promotion.setEndTime(request.getEndTime());

        return promotionRepository.save(promotion);
    }

    @Override
    public PromotionUsageCheckResponse checkPromotionUsageLimit(UUID promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        Integer usageLimit = promotion.getUsageLimit();
        Integer usedCount = promotion.getUsedCount() == null ? 0 : promotion.getUsedCount();

        if (usageLimit == null) {
            return PromotionUsageCheckResponse.builder()
                    .promotionId(promotion.getId())
                    .usageLimit(null)
                    .usedCount(usedCount)
                    .remainingUsage(null)
                    .canUse(true)
                    .message("Promotion has no usage limit")
                    .build();
        }

        int remainingUsage = Math.max(usageLimit - usedCount, 0);
        boolean canUse = usedCount < usageLimit;

        return PromotionUsageCheckResponse.builder()
                .promotionId(promotion.getId())
                .usageLimit(usageLimit)
                .usedCount(usedCount)
                .remainingUsage(remainingUsage)
                .canUse(canUse)
                .message(canUse ? "Promotion can still be used" : "Promotion usage limit exceeded")
                .build();
    }

    @Override
    public Promotion updatePromotionUsage(UUID promotionId, UpdatePromotionUsageRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        int incrementBy = 1;
        if (request != null && request.getIncrementBy() != null) {
            incrementBy = request.getIncrementBy();
        }

        if (incrementBy <= 0) {
            throw new IllegalArgumentException("incrementBy must be greater than 0");
        }

        int currentUsedCount = promotion.getUsedCount() == null ? 0 : promotion.getUsedCount();
        int newUsedCount = currentUsedCount + incrementBy;

        if (promotion.getUsageLimit() != null && newUsedCount > promotion.getUsageLimit()) {
            throw new IllegalArgumentException("Cannot update usage because usage limit will be exceeded");
        }

        promotion.setUsedCount(newUsedCount);
        return promotionRepository.save(promotion);
    }

    private void validateEffectiveTime(PromotionEffectiveTimeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time must not be null");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}