package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.FranchiseResponse;
import franchiseproject.inventory_service.dto.InventoryResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    List<InventoryResponse> viewInventoryByFranchise(UUID franchiseId);

    List<FranchiseResponse> viewInventoryDetail();

    List<InventoryResponse> viewLowStockItems();

    InventoryResponse updateInventoryThreshold(UUID inventoryId, Integer minStock);

    List<InventoryResponse> searchInventory(String productName, UUID franchiseId);

    List<InventoryResponse> filterInventoryByFranchise(String productName);
}
