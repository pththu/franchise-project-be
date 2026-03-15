package franchiseproject.promotion_service.dto;

import franchiseproject.promotion_service.enums.ScopeType;
import lombok.Data;

import java.util.UUID;

@Data
public class CreatePromotionScopeRequest {

    private UUID promotionId;

    private ScopeType scopeType;

    private UUID scopeValue;

}