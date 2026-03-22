package franchiseproject.promotion_service.repository;

import franchiseproject.promotion_service.entity.PromotionUsage;
import franchiseproject.promotion_service.enums.PromotionUsageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, UUID> {

    Optional<PromotionUsage> findByOrderId(UUID orderId);

    List<PromotionUsage> findByStatusAndExpiresAtBefore(
            PromotionUsageStatus status,
            LocalDateTime time
    );
}
