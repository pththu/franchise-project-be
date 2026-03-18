package franchiseproject.promotion_service.service;

import franchiseproject.promotion_service.dto.PromotionResponse;

import java.util.List;

public interface PromotionService {
    List<PromotionResponse> getAllPromotions();
}
