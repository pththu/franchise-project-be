package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.dto.response.InventoryLogDetailResponse;
import franchiseproject.inventory_service.dto.response.InventoryLogResponse;
import franchiseproject.inventory_service.exception.ResourceNotFoundException;
import franchiseproject.inventory_service.entity.InventoryTransaction;
import franchiseproject.inventory_service.repository.InventoryTransactionRepository;
import franchiseproject.inventory_service.service.InventoryLogService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryLogServiceImpl implements InventoryLogService {

    InventoryTransactionRepository inventoryTransactionRepository;

//    @Override
//    public List<InventoryLogResponse> getAllLogs(UUID franchiseId) {
//        List<InventoryTransaction> logs;
//
//        if (franchiseId != null) {
//            logs = inventoryTransactionRepository.findAllByFranchiseId(franchiseId);
//        } else {
//            logs = inventoryTransactionRepository.findAllWithRelations();
//        }
//
//        return logs.stream()
//                .map(this::mapToResponse)
//                .toList();
//    }

//    @Override
//    public InventoryLogDetailResponse getLogDetail(UUID id) {
//        InventoryTransaction log = inventoryTransactionRepository.findDetailById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Inventory log not found"));
//
//        return mapToDetailResponse(log);
//    }

//    private InventoryLogResponse mapToResponse(InventoryTransaction transaction) {
//        FranchiseIngredient ingredient = transaction.getFranchiseIngredient();
//        Franchise franchise = ingredient != null ? ingredient.getFranchise() : null;
//
//        return InventoryLogResponse.builder()
//                .id(transaction.getId())
//                .franchiseId(franchise != null ? franchise.getId() : null)
//                .franchiseName(franchise != null ? franchise.getName() : null)
//                .franchiseIngredientId(transaction.getFranchiseIngredientId())
//                .productId(ingredient != null ? ingredient.getProductId() : null)
//                .quantity(transaction.getQuantity())
//                .beforeQuantity(transaction.getBeforeQuantity())
//                .afterQuantity(transaction.getAfterQuantity())
//                .type(transaction.getType())
//                .threshold(transaction.getThreshold())
//                .alertTriggeredAt(transaction.getAlertTriggeredAt())
//                .withoutThreshold(transaction.getWithoutThreshold())
//                .status(transaction.getStatus())
//                .staffId(transaction.getStaffId())
//                .createdAt(transaction.getCreatedAt())
//                .updatedAt(transaction.getUpdatedAt())
//                .build();
//    }

//    private InventoryLogDetailResponse mapToDetailResponse(InventoryTransaction transaction) {
//        FranchiseIngredient ingredient = transaction.getFranchiseIngredient();
//        Franchise franchise = ingredient != null ? ingredient.getFranchise() : null;
//
//        return InventoryLogDetailResponse.builder()
//                .id(transaction.getId())
//
//                .franchiseId(franchise != null ? franchise.getId() : null)
//                .franchiseName(franchise != null ? franchise.getName() : null)
//                .franchiseAddress(franchise != null ? franchise.getAddress() : null)
//
//                .franchiseIngredientId(transaction.getFranchiseIngredientId())
//                .productId(ingredient != null ? ingredient.getProductId() : null)
//                .currentStock(ingredient != null ? ingredient.getQuantity() : null)
//                .unit(ingredient != null ? ingredient.getUnit() : null)
//                .minStock(ingredient != null ? ingredient.getMinStock() : null)
//
//                .quantity(transaction.getQuantity())
//                .beforeQuantity(transaction.getBeforeQuantity())
//                .afterQuantity(transaction.getAfterQuantity())
//                .type(transaction.getType())
//                .threshold(transaction.getThreshold())
//                .alertTriggeredAt(transaction.getAlertTriggeredAt())
//                .withoutThreshold(transaction.getWithoutThreshold())
//                .status(transaction.getStatus())
//                .staffId(transaction.getStaffId())
//                .createdAt(transaction.getCreatedAt())
//                .updatedAt(transaction.getUpdatedAt())
//                .build();
//    }
}