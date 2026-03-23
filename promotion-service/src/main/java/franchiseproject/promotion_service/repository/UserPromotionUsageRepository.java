package franchiseproject.promotion_service.repository;

import franchiseproject.promotion_service.entity.UserPromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserPromotionUsageRepository extends JpaRepository<UserPromotionUsage, UUID> {

    Optional<UserPromotionUsage> findByUserIdAndPromotionId(UUID userId, UUID promotionId);
}
