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
    Long locationId;
    String locationType;
    Integer quantity;
    Integer reservedQuantity;
    Integer minStock;
    
    // Detailed Product Information (Enriched via Feign)
    String productName;
    String size;
    String color;
    Instant createdAt;
    Instant updatedAt;
}
