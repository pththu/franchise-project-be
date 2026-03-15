package franchiseproject.promotion_service.controller;

import franchiseproject.promotion_service.dto.PromotionEffectiveTimeRequest;
import franchiseproject.promotion_service.dto.PromotionUsageCheckResponse;
import franchiseproject.promotion_service.dto.UpdatePromotionUsageRequest;
import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.service.PromotionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/promotion-tasks")
@RequiredArgsConstructor
public class PromotionTaskController {

    private final PromotionTaskService promotionTaskService;

    /**
     * Task 1: Define Promotion Effective Time
     */
    @PostMapping("/{promotionId}/effective-time")
    public Promotion definePromotionEffectiveTime(
            @PathVariable UUID promotionId,
            @RequestBody PromotionEffectiveTimeRequest request
    ) {
        return promotionTaskService.definePromotionEffectiveTime(promotionId, request);
    }

    /**
     * Task 2: Update Promotion Effective Time
     */
    @PutMapping("/{promotionId}/effective-time")
    public Promotion updatePromotionEffectiveTime(
            @PathVariable UUID promotionId,
            @RequestBody PromotionEffectiveTimeRequest request
    ) {
        return promotionTaskService.updatePromotionEffectiveTime(promotionId, request);
    }

    /**
     * Task 3: Check Promotion Usage Limit
     */
    @GetMapping("/{promotionId}/usage/check")
    public PromotionUsageCheckResponse checkPromotionUsageLimit(@PathVariable UUID promotionId) {
        return promotionTaskService.checkPromotionUsageLimit(promotionId);
    }

    /**
     * Task 4: Update Promotion Usage
     */
    @PatchMapping("/{promotionId}/usage")
    public Promotion updatePromotionUsage(
            @PathVariable UUID promotionId,
            @RequestBody(required = false) UpdatePromotionUsageRequest request
    ) {
        return promotionTaskService.updatePromotionUsage(promotionId, request);
    }
}