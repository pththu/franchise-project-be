package franchiseproject.promotion_service.dto;

import franchiseproject.promotion_service.enums.DiscountType;
import franchiseproject.promotion_service.enums.LoyaltyTier;
import franchiseproject.promotion_service.enums.PromotionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionResponse {
    private UUID id;
    private String name;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LoyaltyTier requiredRank;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer perUserLimit;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private LocalDateTime expiryDate;
    private PromotionStatus status;
}
