package franchiseproject.promotion_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ApplyPromotionUsageRequest {
    private UUID orderId;
    private UUID promotionId;
    private UUID userId;
    private BigDecimal orderValue;
}