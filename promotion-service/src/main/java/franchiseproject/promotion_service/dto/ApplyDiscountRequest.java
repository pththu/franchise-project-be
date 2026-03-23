package franchiseproject.promotion_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ApplyDiscountRequest {
    private UUID userId;
    private UUID franchiseId;
    private BigDecimal orderValue;
    private UUID orderId;
    private String status;
    private UUID promotionId; // optional
    private Integer pointsToUse; // optional
}
