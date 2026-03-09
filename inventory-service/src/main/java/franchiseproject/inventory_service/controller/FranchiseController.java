package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.FranchiseResponse;
import franchiseproject.inventory_service.model.Franchise;
import franchiseproject.inventory_service.service.FranchiseService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/franchises")
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
}