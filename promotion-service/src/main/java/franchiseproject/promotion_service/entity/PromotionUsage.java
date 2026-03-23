package franchiseproject.promotion_service.entity;

import franchiseproject.promotion_service.enums.PromotionUsageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promotion_usage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionUsage {

    @Id
    private UUID id;

    private UUID promotionId;

    private UUID userId;

    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private PromotionUsageStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
}
