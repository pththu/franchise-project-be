package franchiseproject.promotion_service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionResponse {

    UUID id;

    String name;

    BigDecimal discountValue;

    String permission;

    String status;

    Instant updatedAt;

    Integer requiredPoints;

}
