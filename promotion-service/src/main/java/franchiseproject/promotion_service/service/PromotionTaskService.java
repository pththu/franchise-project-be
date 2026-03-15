package franchiseproject.promotion_service.service;

import franchiseproject.promotion_service.dto.PromotionEffectiveTimeRequest;
import franchiseproject.promotion_service.dto.PromotionUsageCheckResponse;
import franchiseproject.promotion_service.dto.UpdatePromotionUsageRequest;
import franchiseproject.promotion_service.entity.Promotion;

import java.util.UUID;

public interface PromotionTaskService {

    Promotion definePromotionEffectiveTime(UUID promotionId, PromotionEffectiveTimeRequest request);

    Promotion updatePromotionEffectiveTime(UUID promotionId, PromotionEffectiveTimeRequest request);

    PromotionUsageCheckResponse checkPromotionUsageLimit(UUID promotionId);

    Promotion updatePromotionUsage(UUID promotionId, UpdatePromotionUsageRequest request);
}