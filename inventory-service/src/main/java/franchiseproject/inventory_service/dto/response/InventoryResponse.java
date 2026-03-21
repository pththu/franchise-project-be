package franchiseproject.inventory_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryResponse {

    UUID id;

    UUID franchiseId;

    String franchiseName;

    String productName;

    Integer quantity;

    String unit;

    Integer minStock;
}
