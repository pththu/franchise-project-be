package franchiseproject.promotion_service.dto;

import franchiseproject.promotion_service.enums.DiscountType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PromotionDiscountResponse {

    @NotNull(message = "promotionUsageId không được trống!")
    private UUID promotionUsageId;

    @NotNull(message = "discount value không được trống!")
    private BigDecimal discountValue;

    @NotNull(message = "discount type không được trống!")
    private DiscountType discountType;
}
