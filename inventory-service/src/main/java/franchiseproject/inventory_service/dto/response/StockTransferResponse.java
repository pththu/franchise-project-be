package franchiseproject.inventory_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockTransferResponse {
    UUID id;
    String transferCode;
    Long fromLocationId;
    String fromLocationName;
    Long toLocationId;
    String toLocationName;
    String type;
    String status;
    UUID referenceRequestId;
    String notes;
    UUID createdBy;
    Instant createdAt;
    Instant updatedAt;
    List<StockTransferItemResponse> items;
}
