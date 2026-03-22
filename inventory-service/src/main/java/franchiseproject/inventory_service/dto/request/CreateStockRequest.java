package franchiseproject.inventory_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateStockRequest {
    @NotNull(message = "Franchise ID is required")
    UUID franchiseId;
    
    String notes;
    UUID createdBy;
    
    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    List<StockRequestItemRequest> items;
}
