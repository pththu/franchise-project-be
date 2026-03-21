package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.ApiResponse;
import franchiseproject.inventory_service.dto.request.ExportInventoryRequest;
import franchiseproject.inventory_service.dto.response.FranchiseOfInventoryResponse;
import franchiseproject.inventory_service.dto.response.InventoryResponse;
import franchiseproject.inventory_service.dto.request.SetMinStockRequest;
import franchiseproject.inventory_service.dto.response.ProductFranchiseResponse;
import franchiseproject.inventory_service.entity.InventoryTransaction;
import franchiseproject.inventory_service.mapper.ProductFranchiseMapper;
import franchiseproject.inventory_service.service.InventoryService;
import franchiseproject.inventory_service.service.ProductFranchiseService;
import franchiseproject.inventory_service.service.impl.InventoryServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {

    InventoryService inventoryService;
    InventoryServiceImpl inventoryServiceImpl;

    ProductFranchiseService productFranchiseService;
    ProductFranchiseMapper productFranchiseMapper;

    @GetMapping
    public ApiResponse<List<ProductFranchiseResponse>> getAll() {
        return ApiResponse.<List<ProductFranchiseResponse>>builder()
                .statusCode(200)
                .message("Get all product franchise")
                .data(productFranchiseService.getAll()
                        .stream()
                        .map(productFranchiseMapper::toProductFranchiseResponse)
                        .toList())
                .build();
    }

    // 1 View Inventory by Franchise
//    @GetMapping("/franchise/{franchiseId}")
//    public List<InventoryResponse> viewInventoryByFranchise(
//            @PathVariable UUID franchiseId) {
//
//        return inventoryService.viewInventoryByFranchise(franchiseId);
//    }

    // 2 View Inventory Detail
//    @GetMapping("/franchise")
//    public List<FranchiseOfInventoryResponse> getAllFranchises() {
//        return inventoryService.viewInventoryDetail();
//    }
//
//    // 3 View Low Stock Items
//    @GetMapping("/low-stock")
//    public List<InventoryResponse> viewLowStockItems() {
//
//        return inventoryService.viewLowStockItems();
//    }

    // 4 Update Inventory Threshold
//    @PutMapping("/{inventoryId}/threshold")
//    public InventoryResponse updateInventoryThreshold(
//            @PathVariable UUID inventoryId,
//            @RequestParam Integer minStock) {
//
//        return inventoryService.updateInventoryThreshold(inventoryId, minStock);
//    }

    // 5 Search Inventory theo chi nhánh
//    @GetMapping("/search")
//    public List<InventoryResponse> searchInventory(
//            @RequestParam String productName,
//            @RequestParam UUID franchiseId) {
//
//        return inventoryService.searchInventory(productName, franchiseId);
//    }

    // 6 Filter Inventory tất cả chi nhánh
//    @GetMapping("/filter")
//    public List<InventoryResponse> filterInventoryByFranchise(
//            @RequestParam String productName) {
//
//        return inventoryService.filterInventoryByFranchise(productName);
//    }

    //Manage warehouse release forms
    //Export Inventory
//    @PostMapping("/export")
//    public ResponseEntity<InventoryTransaction> exportInventory(
//            @RequestBody ExportInventoryRequest request){
//
//        return ResponseEntity.ok(
//                inventoryServiceImpl.exportInventory(request)
//        );
//    }

    //View Export Records
    @GetMapping("/export")
    public ResponseEntity<List<InventoryTransaction>> getExportRecords(){

        return ResponseEntity.ok(
                inventoryServiceImpl.getExportRecords()
        );
    }

    //View Export Record Details
//    @GetMapping("/export/{id}")
//    public ResponseEntity<InventoryTransaction> getExportDetail(
//            @PathVariable UUID id){
//
//        return ResponseEntity.ok(
//                inventoryServiceImpl.getExportRecordDetail(id)
//        );
//    }

    //Edit Export Record
//    @PutMapping("/export/{id}")
//    public ResponseEntity<InventoryTransaction> editExport(
//            @PathVariable UUID id,
//            @RequestBody ExportInventoryRequest request){
//
//        return ResponseEntity.ok(
//                inventoryServiceImpl.editExportRecord(id, request)
//        );
//    }

    //Delete Export Record
//    @DeleteMapping("/export/{id}")
//    public ResponseEntity<String> deleteExport(@PathVariable UUID id){
//
//        inventoryServiceImpl.deleteExportRecord(id);
//
//        return ResponseEntity.ok("Deleted successfully");
//    }


    //Inventory alert management
    //Set Min Stock
//    @PostMapping("/min-stock")
//    public ResponseEntity<InventoryTransaction> setMinStock(
//            @RequestBody SetMinStockRequest request){
//
//        return ResponseEntity.ok(
//                inventoryServiceImpl.setMinStock(request)
//        );
//    }

    //View Stock Alerts
//    @GetMapping("/alerts")
//    public ResponseEntity<List<InventoryTransaction>> getStockAlerts(){
//
//        return ResponseEntity.ok(
//                inventoryServiceImpl.getStockAlerts()
//        );
//    }
}
