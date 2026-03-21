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
public class InventoryLogResponse {
    UUID id;
    UUID franchiseId;
    String franchiseName;
    UUID franchiseIngredientId;
    UUID productId;
    Integer quantity;
    Integer beforeQuantity;
    Integer afterQuantity;
    String type;
    Integer threshold;
    Instant alertTriggeredAt;
    Boolean withoutThreshold;
    String status;
    UUID staffId;
    Instant createdAt;
    Instant updatedAt;
}