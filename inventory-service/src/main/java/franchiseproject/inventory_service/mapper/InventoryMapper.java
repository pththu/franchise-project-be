package franchiseproject.inventory_service.mapper;

import franchiseproject.inventory_service.dto.InventoryResponse;
import franchiseproject.inventory_service.model.FranchiseIngredient;

public class InventoryMapper {

    public static InventoryResponse toResponse(FranchiseIngredient entity, String productName) {

        return InventoryResponse.builder()
                .id(entity.getId())
                .franchiseName(entity.getFranchise().getName())
                .productName(productName)
                .quantity(entity.getQuantity())
                .unit(entity.getUnit())
                .minStock(entity.getMinStock())
                .franchiseId(entity.getFranchise().getId())
                .build();
    }
}
