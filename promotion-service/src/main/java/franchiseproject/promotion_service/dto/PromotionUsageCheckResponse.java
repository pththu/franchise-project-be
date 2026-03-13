package franchiseproject.promotion_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionUsageCheckResponse {
    private UUID promotionId;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer remainingUsage;
    private Boolean canUse;
    private String message;
}