package franchiseproject.promotion_service.dto;

import franchiseproject.promotion_service.enums.CouponStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CouponResponse {

    private UUID id;

    private String couponCode;

    private Integer usageLimit;

    private Integer usedCount;

    private Instant expiryDate;

    private CouponStatus status;

    private UUID promotionId;

}
