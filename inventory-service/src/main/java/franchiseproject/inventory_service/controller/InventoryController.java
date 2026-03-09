package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.FranchiseOfInventoryResponse;
import franchiseproject.inventory_service.dto.InventoryResponse;
import franchiseproject.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // 1 View Inventory by Franchise
    @GetMapping("/franchise/{franchiseId}")
    public List<InventoryResponse> viewInventoryByFranchise(
            @PathVariable UUID franchiseId) {

        return inventoryService.viewInventoryByFranchise(franchiseId);
    }

    // 2 View Inventory Detail
    @GetMapping("/franchise")
    public List<FranchiseOfInventoryResponse> getAllFranchises() {
        return inventoryService.viewInventoryDetail();
    }

    // 3 View Low Stock Items
    @GetMapping("/low-stock")
    public List<InventoryResponse> viewLowStockItems() {

        return inventoryService.viewLowStockItems();
    }

    // 4 Update Inventory Threshold
    @PutMapping("/{inventoryId}/threshold")
    public InventoryResponse updateInventoryThreshold(
            @PathVariable UUID inventoryId,
            @RequestParam Integer minStock) {

        return inventoryService.updateInventoryThreshold(inventoryId, minStock);
    }

    // 5 Search Inventory theo chi nhánh
    @GetMapping("/search")
    public List<InventoryResponse> searchInventory(
            @RequestParam String productName,
            @RequestParam UUID franchiseId) {

        return inventoryService.searchInventory(productName, franchiseId);
    }

    // 6 Filter Inventory tất cả chi nhánh
    @GetMapping("/filter")
    public List<InventoryResponse> filterInventoryByFranchise(
            @RequestParam String productName) {

        return inventoryService.filterInventoryByFranchise(productName);
    }
}
