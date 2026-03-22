package franchiseproject.promotion_service.service;

import franchiseproject.promotion_service.dto.ApplyDiscountRequest;
import franchiseproject.promotion_service.dto.PromotionDiscountResponse;
import franchiseproject.promotion_service.dto.PromotionRequest;
import franchiseproject.promotion_service.dto.PromotionResponse;
import franchiseproject.promotion_service.entity.Promotion;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PromotionService {
    void create(PromotionRequest request);

    List<?> getAll();

    PromotionResponse getById(UUID id);

    void update(UUID id, PromotionRequest request);

    void delete(UUID id);

    public List<Promotion> getAvailablePromotions(UUID userId, UUID franchiseId, BigDecimal orderValue);

    PromotionDiscountResponse applyDiscount(ApplyDiscountRequest req);

    void confirmOrder(UUID orderId, String status);
}
