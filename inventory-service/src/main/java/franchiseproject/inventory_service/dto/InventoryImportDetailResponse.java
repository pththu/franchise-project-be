package franchiseproject.inventory_service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryImportDetailResponse {
    UUID id;
    UUID franchiseId;
    String code;
    String note;
    String status;
    UUID createdBy;
    Instant createdAt;
    Instant updatedAt;
    List<InventoryImportItemResponse> items;
}