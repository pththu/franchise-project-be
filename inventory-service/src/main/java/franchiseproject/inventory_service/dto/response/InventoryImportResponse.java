package franchiseproject.inventory_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryImportResponse {
    UUID id;
    UUID franchiseId;
    String code;
    String note;
    String status;
    UUID createdBy;
    Instant createdAt;
    Instant updatedAt;
}