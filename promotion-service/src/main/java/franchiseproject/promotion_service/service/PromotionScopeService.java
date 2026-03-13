package franchiseproject.promotion_service.service;

import franchiseproject.promotion_service.dto.CreatePromotionScopeRequest;
import franchiseproject.promotion_service.entity.PromotionScope;

import java.util.List;
import java.util.UUID;

public interface PromotionScopeService {

    PromotionScope createScope(CreatePromotionScopeRequest request);

    PromotionScope updateScope(UUID id, CreatePromotionScopeRequest request);

    List<PromotionScope> getScopes(UUID promotionId);

}