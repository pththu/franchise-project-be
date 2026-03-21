package franchiseproject.inventory_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductStockResponse {
    UUID id;
    UUID productVariantId;
    UUID locationId;
    String locationType;
    Integer quantity;
    Integer reservedQuantity;
    Integer minStock;
    Instant createdAt;
    Instant updatedAt;
}
