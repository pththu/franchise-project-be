package franchiseproject.promotion_service.dto;

import franchiseproject.promotion_service.enums.CouponStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class CouponUpdateRequest {

    private Integer usageLimit;

    private Instant expiryDate;

    private CouponStatus status;

}
