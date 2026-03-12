package franchiseproject.inventory_service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryImportItemRequest {
    UUID productId;
    Integer quantity;
    String unit;
}