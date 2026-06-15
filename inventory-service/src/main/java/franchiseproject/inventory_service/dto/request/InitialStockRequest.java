package franchiseproject.inventory_service.dto.request;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitialStockRequest {
    UUID productVariantId;
    Integer quantity;
    UUID locationId; // null means Central Warehouse
    String notes;
    UUID createdBy;
}
