package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.request.CreateInventoryImportRequest;
import franchiseproject.inventory_service.dto.request.UpdateInventoryImportRequest;
import franchiseproject.inventory_service.dto.request.UpdateInventoryImportStatusRequest;
import franchiseproject.inventory_service.service.InventoryImportService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory-imports")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryImportController {

    InventoryImportService inventoryImportService;

//    @PostMapping
//    public ResponseEntity<Map<String, Object>> createImport(@RequestBody CreateInventoryImportRequest request) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Create import record successfully");
//        response.put("data", inventoryImportService.createImport(request));
//        return ResponseEntity.ok(response);
//    }

//    @GetMapping
//    public ResponseEntity<Map<String, Object>> getAllImports() {
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Get all import records successfully");
//        response.put("data", inventoryImportService.getAllImports());
//        return ResponseEntity.ok(response);
//    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Map<String, Object>> getImportById(@PathVariable UUID id) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Get import record detail successfully");
//        response.put("data", inventoryImportService.getImportById(id));
//        return ResponseEntity.ok(response);
//    }

//    @PutMapping("/{id}")
//    public ResponseEntity<Map<String, Object>> updateImport(
//            @PathVariable UUID id,
//            @RequestBody UpdateInventoryImportRequest request
//    ) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Update import record successfully");
//        response.put("data", inventoryImportService.updateImport(id, request));
//        return ResponseEntity.ok(response);
//    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Map<String, Object>> deleteImport(@PathVariable UUID id) {
//        inventoryImportService.deleteImport(id);
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Delete import record successfully");
//        return ResponseEntity.ok(response);
//    }

//    @PatchMapping("/{id}/status")
//    public ResponseEntity<Map<String, Object>> updateImportStatus(
//            @PathVariable UUID id,
//            @RequestBody UpdateInventoryImportStatusRequest request
//    ) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Update import status successfully");
//        response.put("data", inventoryImportService.updateStatus(id, request));
//        return ResponseEntity.ok(response);
//    }
}