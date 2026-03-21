package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.service.InventoryLogService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory-logs")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryLogController {

    InventoryLogService inventoryLogService;

//    @GetMapping
//    public ResponseEntity<Map<String, Object>> getAllLogs(
//            @RequestParam(required = false) UUID franchiseId
//    ) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Get inventory logs successfully");
//        response.put("data", inventoryLogService.getAllLogs(franchiseId));
//        return ResponseEntity.ok(response);
//    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Map<String, Object>> getLogDetail(@PathVariable UUID id) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Get inventory log detail successfully");
//        response.put("data", inventoryLogService.getLogDetail(id));
//        return ResponseEntity.ok(response);
//    }
}