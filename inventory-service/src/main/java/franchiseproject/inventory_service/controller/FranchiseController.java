package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.CreateFranchiseRequest;
import franchiseproject.inventory_service.dto.UpdateFranchiseRequest;
import franchiseproject.inventory_service.dto.UpdateStatusRequest;
import franchiseproject.inventory_service.dto.FranchiseResponse;
import franchiseproject.inventory_service.model.Franchise;
import franchiseproject.inventory_service.service.FranchiseService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FranchiseController {

    FranchiseService franchiseService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFranchises() {
        List<Franchise> franchiseList = franchiseService.getAll();

        List<FranchiseResponse> data = franchiseList.stream()
                .map(this::toResponse)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get all franchises successfully");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public String test() {
        return "franchise controller ok";
    }

    private FranchiseResponse toResponse(Franchise franchise) {
        return FranchiseResponse.builder()
                .id(franchise.getId())
                .name(franchise.getName())
                .address(franchise.getAddress())
                .openedAt(franchise.getOpenedAt())
                .closedAt(franchise.getClosedAt())
                .isActive(franchise.getIsActive())
                .createdAt(franchise.getCreatedAt())
                .updatedAt(franchise.getUpdatedAt())
                .build();
    }

    @PostMapping
    public ResponseEntity<Franchise> create(
            @Valid @RequestBody CreateFranchiseRequest request) {

        return ResponseEntity.ok(franchiseService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Franchise> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(franchiseService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Franchise> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFranchiseRequest request) {

        return ResponseEntity.ok(franchiseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {

        franchiseService.delete(id);
        return ResponseEntity.ok("Delete successful");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Franchise> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {

        return ResponseEntity.ok(
                franchiseService.updateStatus(id, request.getIsActive())
        );
    }
}