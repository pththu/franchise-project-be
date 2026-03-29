package franchiseproject.inventory_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockTransferItemResponse {
    UUID id;
    UUID productVariantId;
    String productVariantName;
    Integer quantity;
    Integer currentQuantity;
}
