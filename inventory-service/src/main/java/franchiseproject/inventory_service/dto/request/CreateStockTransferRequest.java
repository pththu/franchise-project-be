package franchiseproject.inventory_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateStockTransferRequest {
    UUID fromLocationId;
    UUID toLocationId;
    String type; // WAREHOUSE_TO_FRANCHISE etc.
    String notes;
    UUID createdBy;
    UUID referenceRequestId;
    List<StockTransferItemRequest> items;
}
