package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.dto.request.CreateStockTransferRequest;
import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.StockTransferItemResponse;
import franchiseproject.inventory_service.dto.response.StockTransferResponse;
import franchiseproject.inventory_service.entity.InventoryTransaction;
import franchiseproject.inventory_service.entity.ProductStock;
import franchiseproject.inventory_service.entity.StockTransfer;
import franchiseproject.inventory_service.entity.StockTransferItem;
import franchiseproject.inventory_service.enums.TransferStatus;
import franchiseproject.inventory_service.enums.TransferType;
import franchiseproject.inventory_service.exception.AppException;
import franchiseproject.inventory_service.exception.ErrorCode;
import franchiseproject.inventory_service.repository.InventoryTransactionRepository;
import franchiseproject.inventory_service.repository.ProductStockRepository;
import franchiseproject.inventory_service.repository.StockTransferRepository;
import franchiseproject.inventory_service.service.StockTransferService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockTransferServiceImpl implements StockTransferService {

    StockTransferRepository stockTransferRepository;
    ProductStockRepository productStockRepository;
    InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    @Transactional
    public StockTransferResponse createTransfer(CreateStockTransferRequest request) {
        String code = "TRN-" + System.currentTimeMillis();
        TransferType type = TransferType.valueOf(request.getType());

        StockTransfer transfer = StockTransfer.builder()
                .transferCode(code)
                .fromLocationId(request.getFromLocationId())
                .toLocationId(request.getToLocationId())
                .type(type)
                .status(TransferStatus.IN_TRANSIT)
                .notes(request.getNotes())
                .createdBy(request.getCreatedBy())
                .items(new ArrayList<>())
                .build();

        List<StockTransferItem> items = request.getItems().stream().map(itemReq -> {
            // Trừ kho nguồn
            Optional<ProductStock> sourceStockOpt = productStockRepository.findByProductVariantIdAndLocationId(
                    itemReq.getProductVariantId(), request.getFromLocationId());
            
            if (sourceStockOpt.isEmpty() || sourceStockOpt.get().getQuantity() < itemReq.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }

            ProductStock sourceStock = sourceStockOpt.get();
            int before = sourceStock.getQuantity();
            sourceStock.setQuantity(before - itemReq.getQuantity());
            productStockRepository.save(sourceStock);

            // Record Transfer Transaction Log for Source
            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .productStock(sourceStock)
                    .changeQuantity(-itemReq.getQuantity())
                    .beforeQuantity(before)
                    .afterQuantity(sourceStock.getQuantity())
                    .type("TRANSFER_OUT")
                    .status("COMPLETED")
                    .referenceType("TRANSFER")
                    .createdBy(request.getCreatedBy())
                    .build());

            // Tăng kho đích 
            Optional<ProductStock> destStockOpt = productStockRepository.findByProductVariantIdAndLocationId(
                    itemReq.getProductVariantId(), request.getToLocationId());
            
            ProductStock destStock;
            int destBefore = 0;
            if (destStockOpt.isPresent()) {
                 destStock = destStockOpt.get();
                 destBefore = destStock.getQuantity();
                 destStock.setQuantity(destBefore + itemReq.getQuantity());
            } else {
                 destStock = ProductStock.builder()
                         .productVariantId(itemReq.getProductVariantId())
                         .locationId(request.getToLocationId())
                         .locationType("FRANCHISE")
                         .quantity(itemReq.getQuantity())
                         .reservedQuantity(0)
                         .minStock(5)
                         .build();
            }
            productStockRepository.save(destStock);

            // Record Transfer Transaction Log for Destination
            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .productStock(destStock)
                    .changeQuantity(itemReq.getQuantity())
                    .beforeQuantity(destBefore)
                    .afterQuantity(destStock.getQuantity())
                    .type("TRANSFER_IN")
                    .status("COMPLETED")
                    .referenceType("TRANSFER")
                    .createdBy(request.getCreatedBy())
                    .build());

            return StockTransferItem.builder()
                    .productVariantId(itemReq.getProductVariantId())
                    .quantity(itemReq.getQuantity())
                    .stockTransfer(transfer)
                    .build();
        }).collect(Collectors.toList());

        transfer.setItems(items);
        StockTransfer saved = stockTransferRepository.save(transfer);

        return mapToResponse(saved);
    }

    @Override
    public PageResponse<StockTransferResponse> getAllTransfers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StockTransfer> p = stockTransferRepository.findAll(pageable);

        return PageResponse.<StockTransferResponse>builder()
                .content(p.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageNo(p.getNumber())
                .pageSize(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }

    @Override
    public StockTransferResponse getTransferById(UUID id) {
        StockTransfer t = stockTransferRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return mapToResponse(t);
    }

    private StockTransferResponse mapToResponse(StockTransfer t) {
        return StockTransferResponse.builder()
                .id(t.getId())
                .transferCode(t.getTransferCode())
                .fromLocationId(t.getFromLocationId())
                .toLocationId(t.getToLocationId())
                .type(t.getType().name())
                .status(t.getStatus().name())
                .referenceRequestId(t.getReferenceRequestId())
                .notes(t.getNotes())
                .createdBy(t.getCreatedBy())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .items(t.getItems().stream().map(i -> StockTransferItemResponse.builder()
                        .id(i.getId())
                        .productVariantId(i.getProductVariantId())
                        .quantity(i.getQuantity())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
