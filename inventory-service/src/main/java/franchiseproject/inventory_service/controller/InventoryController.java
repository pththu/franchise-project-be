package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.ExportInventoryRequest;
import franchiseproject.inventory_service.dto.FranchiseOfInventoryResponse;
import franchiseproject.inventory_service.dto.InventoryResponse;
import franchiseproject.inventory_service.dto.SetMinStockRequest;
import franchiseproject.inventory_service.model.FranchiseIngredient;
import franchiseproject.inventory_service.model.InventoryTransaction;
import franchiseproject.inventory_service.service.InventoryService;
import franchiseproject.inventory_service.service.impl.InventoryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryServiceImpl inventoryServiceImpl;

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
    //Manage warehouse release forms
    //Export Inventory
    @PostMapping("/export")
    public ResponseEntity<InventoryTransaction> exportInventory(
            @RequestBody ExportInventoryRequest request){

        return ResponseEntity.ok(
                inventoryServiceImpl.exportInventory(request)
        );
    }

    //View Export Records
    @GetMapping("/export")
    public ResponseEntity<List<InventoryTransaction>> getExportRecords(){

        return ResponseEntity.ok(
                inventoryServiceImpl.getExportRecords()
        );
    }

    //View Export Record Details
    @GetMapping("/export/{id}")
    public ResponseEntity<InventoryTransaction> getExportDetail(
            @PathVariable UUID id){

        return ResponseEntity.ok(
                inventoryServiceImpl.getExportRecordDetail(id)
        );
    }

    //Edit Export Record
    @PutMapping("/export/{id}")
    public ResponseEntity<InventoryTransaction> editExport(
            @PathVariable UUID id,
            @RequestBody ExportInventoryRequest request){

        return ResponseEntity.ok(
                inventoryServiceImpl.editExportRecord(id, request)
        );
    }

    //Delete Export Record
    @DeleteMapping("/export/{id}")
    public ResponseEntity<String> deleteExport(@PathVariable UUID id){

        inventoryServiceImpl.deleteExportRecord(id);

        return ResponseEntity.ok("Deleted successfully");
    }


    //Inventory alert management
    //Set Min Stock
    @PostMapping("/min-stock")
    public ResponseEntity<InventoryTransaction> setMinStock(
            @RequestBody SetMinStockRequest request){

        return ResponseEntity.ok(
                inventoryServiceImpl.setMinStock(request)
        );
    }

    //View Stock Alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<InventoryTransaction>> getStockAlerts(){

        return ResponseEntity.ok(
                inventoryServiceImpl.getStockAlerts()
        );
    }

    //View Low Stock by Franchise
    @GetMapping("/low-stock/{franchiseId}")
    public ResponseEntity<List<FranchiseIngredient>> getLowStockByFranchise(
            @PathVariable UUID franchiseId){

        return ResponseEntity.ok(
                inventoryServiceImpl.getLowStockByFranchise(franchiseId)
        );
    }
}
