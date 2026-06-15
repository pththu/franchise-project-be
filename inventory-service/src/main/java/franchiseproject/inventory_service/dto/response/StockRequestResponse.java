package franchiseproject.inventory_service.dto.response;

import franchiseproject.inventory_service.enums.StockRequestStatus;
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
public class StockRequestResponse {
    UUID id;
    String requestCode;
    UUID franchiseId;
    StockRequestStatus status;
    String notes;
    UUID createdBy;
    UUID approvedBy;
    Instant createdAt;
    Instant updatedAt;
    UUID sourceLocationId;
    List<StockRequestItemResponse> items;
}
