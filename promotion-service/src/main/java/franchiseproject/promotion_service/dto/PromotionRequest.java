package franchiseproject.promotion_service.dto;

import franchiseproject.promotion_service.enums.DiscountType;
import franchiseproject.promotion_service.enums.LoyaltyTier;
import franchiseproject.promotion_service.enums.PromotionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionRequest {
    private String name;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LoyaltyTier requiredRank;
    private Integer usageLimit;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private LocalDateTime expiryDate;
    private PromotionStatus status;
    private Integer perUserLimit;
}
