package franchiseproject.inventory_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockSubtractRequest {
    UUID locationId;
    List<StockRequestItemRequest> items;
    boolean fromReserved; // true nếu trừ từ số lượng đã giữ chỗ, false nếu trừ thẳng (POS)
    String referenceId;
    String referenceType;
    String createdBy;
}
