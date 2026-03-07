package franchiseproject.inventory_service.service.impl;


import franchiseproject.inventory_service.dto.FranchiseResponse;
import franchiseproject.inventory_service.dto.InventoryResponse;
import franchiseproject.inventory_service.mapper.InventoryMapper;
import franchiseproject.inventory_service.model.FranchiseIngredient;
import franchiseproject.inventory_service.repository.FranchiseIngredientRepository;
import franchiseproject.inventory_service.repository.FranchiseRepository;
import franchiseproject.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryImpl implements InventoryService {

    private final FranchiseIngredientRepository repository;
    private final FranchiseRepository franchiseRepository;

    // 1 View Inventory by Franchise
    @Override
    public List<InventoryResponse> viewInventoryByFranchise(UUID franchiseId) {

        return repository.findByFranchiseId(franchiseId)
                .stream()
                .map(i -> InventoryMapper.toResponse(i, i.getProductName()))
                .toList();
    }

    // 2 View Inventory Details
    @Override
    public List<FranchiseResponse> viewInventoryDetail() {

        return franchiseRepository.findAll()
                .stream()
                .map(f -> FranchiseResponse.builder()
                        .id(f.getId())
                        .franchiseName(f.getName())
                        .build())
                .toList();
    }

    // 3 View Low Stock Items
    @Override
    public List<InventoryResponse> viewLowStockItems() {

        return repository.findAll()
                .stream()
                .filter(i -> i.getQuantity() <= i.getMinStock())
                .map(i -> InventoryMapper.toResponse(i, i.getProductName()))
                .toList();
    }

    // 4 Update Inventory Threshold
    @Override
    public InventoryResponse updateInventoryThreshold(UUID inventoryId, Integer minStock) {

        FranchiseIngredient inventory = repository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setMinStock(minStock);
        repository.save(inventory);

        return InventoryMapper.toResponse(inventory, inventory.getProductName());
    }

    // 5 Search Inventory theo chi nhánh
    @Override
    public List<InventoryResponse> searchInventory(String productName, UUID franchiseId) {

        return repository
                .findByProductNameContainingIgnoreCaseAndFranchise_Id(productName, franchiseId)
                .stream()
                .map(i -> InventoryMapper.toResponse(i, i.getProductName()))
                .toList();
    }
    // 6 Filter Inventory tất cả chi nhánh
    @Override
    public List<InventoryResponse> filterInventoryByFranchise(String productName) {

        return repository.findByProductNameContainingIgnoreCase(productName)
                .stream()
                .map(i -> InventoryMapper.toResponse(i, i.getProductName()))
                .toList();
    }
}