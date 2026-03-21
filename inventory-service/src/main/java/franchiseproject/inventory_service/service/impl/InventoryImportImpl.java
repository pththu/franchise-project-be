package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.dto.request.CreateInventoryImportRequest;
import franchiseproject.inventory_service.dto.request.InventoryImportItemRequest;
import franchiseproject.inventory_service.dto.request.UpdateInventoryImportRequest;
import franchiseproject.inventory_service.dto.request.UpdateInventoryImportStatusRequest;
import franchiseproject.inventory_service.dto.response.InventoryImportDetailResponse;
import franchiseproject.inventory_service.dto.response.InventoryImportItemResponse;
import franchiseproject.inventory_service.dto.response.InventoryImportResponse;
import franchiseproject.inventory_service.enums.ImportStatus;
import franchiseproject.inventory_service.exception.BadRequestException;
import franchiseproject.inventory_service.exception.ResourceNotFoundException;
import franchiseproject.inventory_service.entity.*;
import franchiseproject.inventory_service.repository.*;
import franchiseproject.inventory_service.service.InventoryImportService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryImportImpl implements InventoryImportService {

    InventoryImportRepository inventoryImportRepository;
    InventoryImportItemRepository inventoryImportItemRepository;
    InventoryTransactionRepository inventoryTransactionRepository;

//    @Override
//    @Transactional
//    public InventoryImportDetailResponse createImport(CreateInventoryImportRequest request) {
//        validateCreateRequest(request);
//
//        if (inventoryImportRepository.existsByCode(request.getCode())) {
//            throw new BadRequestException("Import code already exists");
//        }
//
//        Franchise franchise = franchiseRepository.findById(request.getFranchiseId())
//                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found"));
//
//        ImportStatus requestedStatus = parseStatus(
//                request.getStatus() == null || request.getStatus().isBlank() ? "DRAFT" : request.getStatus()
//        );
//
//        InventoryImport inventoryImport = InventoryImport.builder()
//                .franchise(franchise)
//                .code(request.getCode())
//                .note(request.getNote())
//                .status(ImportStatus.DRAFT.name())
//                .createdBy(request.getCreatedBy())
//                .items(new ArrayList<>())
//                .build();
//
//        for (InventoryImportItemRequest itemRequest : request.getItems()) {
//            InventoryImportItem item = InventoryImportItem.builder()
//                    .inventoryImport(inventoryImport)
//                    .productId(itemRequest.getProductId())
//                    .quantity(itemRequest.getQuantity())
//                    .unit(itemRequest.getUnit())
//                    .build();
//            inventoryImport.getItems().add(item);
//        }
//
//        inventoryImport = inventoryImportRepository.save(inventoryImport);
//
//        if (requestedStatus == ImportStatus.CONFIRMED) {
//            applyConfirm(inventoryImport, request.getCreatedBy());
//        } else if (requestedStatus == ImportStatus.CANCELLED) {
//            inventoryImport.setStatus(ImportStatus.CANCELLED.name());
//            inventoryImport = inventoryImportRepository.save(inventoryImport);
//        }
//
//        return mapToDetailResponse(
//                inventoryImportRepository.findById(inventoryImport.getId())
//                        .orElseThrow(() -> new ResourceNotFoundException("Import record not found"))
//        );
//    }

//    @Override
//    public List<InventoryImportResponse> getAllImports() {
//        return inventoryImportRepository.findAll()
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }

//    @Override
//    public InventoryImportDetailResponse getImportById(UUID id) {
//        InventoryImport inventoryImport = inventoryImportRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Import record not found"));
//        return mapToDetailResponse(inventoryImport);
//    }

//    @Override
//    @Transactional
//    public InventoryImportDetailResponse updateImport(UUID id, UpdateInventoryImportRequest request) {
//        validateUpdateRequest(request);
//
//        InventoryImport inventoryImport = inventoryImportRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Import record not found"));
//
//        if (ImportStatus.CONFIRMED.name().equals(inventoryImport.getStatus())) {
//            throw new BadRequestException("Confirmed import record cannot be edited");
//        }
//
//        if (inventoryImportRepository.existsByCodeAndIdNot(request.getCode(), id)) {
//            throw new BadRequestException("Import code already exists");
//        }
//
//        Franchise franchise = franchiseRepository.findById(request.getFranchiseId())
//                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found"));
//
//        inventoryImport.setFranchise(franchise);
//        inventoryImport.setCode(request.getCode());
//        inventoryImport.setNote(request.getNote());
//
//        inventoryImport.getItems().clear();
//
//        for (InventoryImportItemRequest itemRequest : request.getItems()) {
//            InventoryImportItem item = InventoryImportItem.builder()
//                    .inventoryImport(inventoryImport)
//                    .productId(itemRequest.getProductId())
//                    .quantity(itemRequest.getQuantity())
//                    .unit(itemRequest.getUnit())
//                    .build();
//            inventoryImport.getItems().add(item);
//        }
//
//        inventoryImport = inventoryImportRepository.save(inventoryImport);
//
//        return mapToDetailResponse(inventoryImport);
//    }

//    @Override
//    @Transactional
//    public void deleteImport(UUID id) {
//        InventoryImport inventoryImport = inventoryImportRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Import record not found"));
//
//        if (ImportStatus.CONFIRMED.name().equals(inventoryImport.getStatus())) {
//            throw new BadRequestException("Confirmed import record cannot be deleted");
//        }
//
//        inventoryImportRepository.delete(inventoryImport);
//    }

//    @Override
//    @Transactional
//    public InventoryImportDetailResponse updateStatus(UUID id, UpdateInventoryImportStatusRequest request) {
//        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
//            throw new BadRequestException("Status is required");
//        }
//
//        InventoryImport inventoryImport = inventoryImportRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Import record not found"));
//
//        ImportStatus currentStatus = parseStatus(inventoryImport.getStatus());
//        ImportStatus targetStatus = parseStatus(request.getStatus());
//
//        if (currentStatus == targetStatus) {
//            return mapToDetailResponse(inventoryImport);
//        }
//
//        if (currentStatus == ImportStatus.CONFIRMED) {
//            throw new BadRequestException("Confirmed import record cannot change status");
//        }
//
//        if (currentStatus == ImportStatus.CANCELLED && targetStatus == ImportStatus.CONFIRMED) {
//            throw new BadRequestException("Cancelled import record cannot be confirmed");
//        }
//
//        if (targetStatus == ImportStatus.CONFIRMED) {
//            applyConfirm(inventoryImport, request.getStaffId());
//        } else if (targetStatus == ImportStatus.CANCELLED) {
//            inventoryImport.setStatus(ImportStatus.CANCELLED.name());
//            inventoryImport = inventoryImportRepository.save(inventoryImport);
//        } else {
//            inventoryImport.setStatus(ImportStatus.DRAFT.name());
//            inventoryImport = inventoryImportRepository.save(inventoryImport);
//        }
//
//        return mapToDetailResponse(
//                inventoryImportRepository.findById(id)
//                        .orElseThrow(() -> new ResourceNotFoundException("Import record not found"))
//        );
//    }

//    private void applyConfirm(InventoryImport inventoryImport, UUID staffId) {
//        List<InventoryImportItem> items = inventoryImport.getItems();
//
//        if (items == null || items.isEmpty()) {
//            throw new BadRequestException("Import record must have at least one item");
//        }
//
//        UUID franchiseId = inventoryImport.getFranchise().getId();
//
//        for (InventoryImportItem item : items) {
//            FranchiseIngredient franchiseIngredient = franchiseIngredientRepository
//                    .findByFranchise_IdAndProductId(franchiseId, item.getProductId())
//                    .orElseGet(() -> franchiseIngredientRepository.save(
//                            FranchiseIngredient.builder()
//                                    .franchise(inventoryImport.getFranchise())
//                                    .productId(item.getProductId())
//                                    .quantity(0)
//                                    .unit(item.getUnit())
//                                    .minStock(0)
//                                    .build()
//                    ));
//
//            int beforeQty = franchiseIngredient.getQuantity() == null ? 0 : franchiseIngredient.getQuantity();
//            int importQty = item.getQuantity() == null ? 0 : item.getQuantity();
//            int afterQty = beforeQty + importQty;
//
//            franchiseIngredient.setQuantity(afterQty);
//            if (item.getUnit() != null && !item.getUnit().isBlank()) {
//                franchiseIngredient.setUnit(item.getUnit());
//            }
//
//            franchiseIngredientRepository.save(franchiseIngredient);
//
//            InventoryTransaction transaction = InventoryTransaction.builder()
//                    .franchiseIngredientId(franchiseIngredient.getId())
//                    .quantity(importQty)
//                    .beforeQuantity(beforeQty)
//                    .afterQuantity(afterQty)
//                    .type("IMPORT")
//                    .threshold(franchiseIngredient.getMinStock())
//                    .alertTriggeredAt(null)
//                    .withoutThreshold(false)
//                    .status("CONFIRMED")
//                    .staffId(staffId)
//                    .build();
//
//            inventoryTransactionRepository.save(transaction);
//        }
//
//        inventoryImport.setStatus(ImportStatus.CONFIRMED.name());
//        inventoryImportRepository.save(inventoryImport);
//    }

//    private void validateCreateRequest(CreateInventoryImportRequest request) {
//        if (request == null) {
//            throw new BadRequestException("Request body is required");
//        }
//        if (request.getFranchiseId() == null) {
//            throw new BadRequestException("Franchise id is required");
//        }
//        if (request.getCode() == null || request.getCode().isBlank()) {
//            throw new BadRequestException("Import code is required");
//        }
//        validateItems(request.getItems());
//    }

//    private void validateUpdateRequest(UpdateInventoryImportRequest request) {
//        if (request == null) {
//            throw new BadRequestException("Request body is required");
//        }
//        if (request.getFranchiseId() == null) {
//            throw new BadRequestException("Franchise id is required");
//        }
//        if (request.getCode() == null || request.getCode().isBlank()) {
//            throw new BadRequestException("Import code is required");
//        }
//        validateItems(request.getItems());
//    }

//    private void validateItems(List<InventoryImportItemRequest> items) {
//        if (items == null || items.isEmpty()) {
//            throw new BadRequestException("Import items must not be empty");
//        }
//
//        for (InventoryImportItemRequest item : items) {
//            if (item.getProductId() == null) {
//                throw new BadRequestException("Product id is required");
//            }
//            if (item.getQuantity() == null || item.getQuantity() <= 0) {
//                throw new BadRequestException("Quantity must be greater than 0");
//            }
//        }
//    }

//    private ImportStatus parseStatus(String status) {
//        try {
//            return ImportStatus.valueOf(status.toUpperCase());
//        } catch (Exception e) {
//            throw new BadRequestException("Invalid status. Allowed values: DRAFT, CONFIRMED, CANCELLED");
//        }
//    }

//    private InventoryImportResponse mapToResponse(InventoryImport inventoryImport) {
//        return InventoryImportResponse.builder()
//                .id(inventoryImport.getId())
//                .franchiseId(inventoryImport.getFranchise().getId())
//                .code(inventoryImport.getCode())
//                .note(inventoryImport.getNote())
//                .status(inventoryImport.getStatus())
//                .createdBy(inventoryImport.getCreatedBy())
//                .createdAt(inventoryImport.getCreatedAt())
//                .updatedAt(inventoryImport.getUpdatedAt())
//                .build();
//    }

//    private InventoryImportDetailResponse mapToDetailResponse(InventoryImport inventoryImport) {
//        return InventoryImportDetailResponse.builder()
//                .id(inventoryImport.getId())
//                .franchiseId(inventoryImport.getFranchise().getId())
//                .code(inventoryImport.getCode())
//                .note(inventoryImport.getNote())
//                .status(inventoryImport.getStatus())
//                .createdBy(inventoryImport.getCreatedBy())
//                .createdAt(inventoryImport.getCreatedAt())
//                .updatedAt(inventoryImport.getUpdatedAt())
//                .items(
//                        inventoryImport.getItems().stream()
//                                .map(item -> InventoryImportItemResponse.builder()
//                                        .id(item.getId())
//                                        .productId(item.getProductId())
//                                        .quantity(item.getQuantity())
//                                        .unit(item.getUnit())
//                                        .build())
//                                .toList()
//                )
//                .build();
//    }
}