package franchiseproject.promotion_service.repository;

import franchiseproject.promotion_service.entity.PromotionScope;
import franchiseproject.promotion_service.enums.ScopeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PromotionScopeRepository extends JpaRepository<PromotionScope, UUID> {

    List<PromotionScope> findByPromotionId(UUID promotionId);

    boolean existsByPromotionIdAndScopeTypeAndScopeValue(
            UUID promotionId,
            ScopeType scopeType,
            UUID scopeValue
    );
}