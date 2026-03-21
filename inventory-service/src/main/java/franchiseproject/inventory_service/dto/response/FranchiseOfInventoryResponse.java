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
public class FranchiseOfInventoryResponse {

    private UUID id;

    private String productName;

    private String franchiseName;

    private Integer quantity;

    private String unit;

    private Integer minStock;
}
