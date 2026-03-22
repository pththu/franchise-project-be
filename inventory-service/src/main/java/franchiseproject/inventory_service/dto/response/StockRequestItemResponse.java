package franchiseproject.inventory_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockRequestItemResponse {
    UUID id;
    UUID productVariantId;
    Integer quantity;
    
    // Detailed Product Information (Enriched via Feign)
    String productName;
    String size;
    String color;
    Integer currentQuantity;
}
