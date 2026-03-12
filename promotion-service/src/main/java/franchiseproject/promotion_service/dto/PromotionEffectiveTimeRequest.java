package franchiseproject.promotion_service.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class PromotionEffectiveTimeRequest {
    private Instant startTime;
    private Instant endTime;
}