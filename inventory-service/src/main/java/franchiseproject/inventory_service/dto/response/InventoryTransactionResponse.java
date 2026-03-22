package franchiseproject.inventory_service.dto.response;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryTransactionResponse {
    UUID id;
    UUID productVariantId;
    Long locationId;
    Integer changeQuantity;
    Integer beforeQuantity;
    Integer afterQuantity;
    String type;
    UUID referenceId;
    String referenceType;
    Instant createdAt;
}
