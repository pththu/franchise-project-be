package franchiseproject.promotion_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_promotion_usage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPromotionUsage {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "promotion_id", nullable = false)
    private UUID promotionId;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
