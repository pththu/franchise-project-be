package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.ExportInventoryRequest;
import franchiseproject.inventory_service.dto.SetMinStockRequest;
import franchiseproject.inventory_service.model.FranchiseIngredient;
import franchiseproject.inventory_service.model.InventoryTransaction;
import franchiseproject.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    //Manage warehouse release forms
    //Export Inventory
    @PostMapping("/export")
    public ResponseEntity<InventoryTransaction> exportInventory(
            @RequestBody ExportInventoryRequest request){

        return ResponseEntity.ok(
                inventoryService.exportInventory(request)
        );
    }

    //View Export Records
    @GetMapping("/export")
    public ResponseEntity<List<InventoryTransaction>> getExportRecords(){

        return ResponseEntity.ok(
                inventoryService.getExportRecords()
        );
    }

    //View Export Record Details
    @GetMapping("/export/{id}")
    public ResponseEntity<InventoryTransaction> getExportDetail(
            @PathVariable UUID id){

        return ResponseEntity.ok(
                inventoryService.getExportRecordDetail(id)
        );
    }

    //Edit Export Record
    @PutMapping("/export/{id}")
    public ResponseEntity<InventoryTransaction> editExport(
            @PathVariable UUID id,
            @RequestBody ExportInventoryRequest request){

        return ResponseEntity.ok(
                inventoryService.editExportRecord(id, request)
        );
    }

    //Delete Export Record
    @DeleteMapping("/export/{id}")
    public ResponseEntity<String> deleteExport(@PathVariable UUID id){

        inventoryService.deleteExportRecord(id);

        return ResponseEntity.ok("Deleted successfully");
    }


    //Inventory alert management
    //Set Min Stock
    @PostMapping("/min-stock")
    public ResponseEntity<InventoryTransaction> setMinStock(
            @RequestBody SetMinStockRequest request){

        return ResponseEntity.ok(
                inventoryService.setMinStock(request)
        );
    }

    //View Stock Alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<InventoryTransaction>> getStockAlerts(){

        return ResponseEntity.ok(
                inventoryService.getStockAlerts()
        );
    }

    //View Low Stock by Franchise
    @GetMapping("/low-stock/{franchiseId}")
    public ResponseEntity<List<FranchiseIngredient>> getLowStockByFranchise(
            @PathVariable UUID franchiseId){

        return ResponseEntity.ok(
                inventoryService.getLowStockByFranchise(franchiseId)
        );
    }
}