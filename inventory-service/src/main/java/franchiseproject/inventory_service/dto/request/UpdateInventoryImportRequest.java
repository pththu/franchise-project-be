package franchiseproject.inventory_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateInventoryImportRequest {
    UUID franchiseId;
    String code;
    String note;
    List<InventoryImportItemRequest> items;
}