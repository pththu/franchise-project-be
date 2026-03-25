package franchiseproject.promotion_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ApplyDiscountRequest {
    private UUID customerId;
    private UUID franchiseId;
    private BigDecimal orderValue;
    private UUID orderId;
    private UUID promotionId; // optional
}
