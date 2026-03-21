package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.response.FranchiseOfInventoryResponse;
import franchiseproject.inventory_service.dto.response.InventoryResponse;
import franchiseproject.inventory_service.entity.InventoryTransaction;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    List<InventoryTransaction> getExportRecords();
    InventoryTransaction getExportRecordDetail(UUID id);

//    List<InventoryResponse> viewInventoryByFranchise(UUID franchiseId);
//    List<FranchiseOfInventoryResponse> viewInventoryDetail();
//    List<InventoryResponse> viewLowStockItems();
//    InventoryResponse updateInventoryThreshold(UUID inventoryId, Integer minStock);
//    List<InventoryResponse> searchInventory(String productName, UUID franchiseId);
//    List<InventoryResponse> filterInventoryByFranchise(String productName);
//    List<FranchiseIngredient> getLowStockByFranchise(UUID franchiseId);
//    InventoryTransaction editExportRecord(UUID id, ExportInventoryRequest request);
//    void deleteExportRecord(UUID id);
//    InventoryTransaction setMinStock(SetMinStockRequest request);
//    List<InventoryTransaction> getStockAlerts();
//    InventoryTransaction exportInventory(ExportInventoryRequest request);

}
