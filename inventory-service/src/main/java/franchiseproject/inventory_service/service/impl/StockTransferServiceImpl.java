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
import franchiseproject.inventory_service.client.ProductClient;
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
    ProductClient productClient;

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
                .status(TransferStatus.PENDING)
                .referenceRequestId(request.getReferenceRequestId())
                .notes(request.getNotes())
                .createdBy(request.getCreatedBy())
                .items(new ArrayList<>())
                .build();

        List<StockTransferItem> items = request.getItems().stream().map(itemReq -> {
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
    public PageResponse<StockTransferResponse> getAllTransfers(int page, int size, UUID fromLocationId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StockTransfer> p = (fromLocationId != null)
                ? stockTransferRepository.findByFromLocationId(fromLocationId, pageable)
                : stockTransferRepository.findAll(pageable);

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

    @Override
    @org.springframework.transaction.annotation.Transactional
    public StockTransferResponse shipTransfer(UUID id) {
        StockTransfer t = stockTransferRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (t.getStatus() != TransferStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        for (franchiseproject.inventory_service.entity.StockTransferItem item : t.getItems()) {
            ProductStock sourceStock = productStockRepository.findByProductVariantIdAndLocationId(
                    item.getProductVariantId(), t.getFromLocationId())
                    .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_STOCK));
            if (sourceStock.getQuantity() < item.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
            int before = sourceStock.getQuantity();
            sourceStock.setQuantity(before - item.getQuantity());
            productStockRepository.save(sourceStock);

            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .productStock(sourceStock)
                    .changeQuantity(-item.getQuantity())
                    .beforeQuantity(before)
                    .afterQuantity(sourceStock.getQuantity())
                    .type("TRANSFER_OUT")
                    .status("COMPLETED")
                    .referenceType("TRANSFER")
                    .createdBy(t.getCreatedBy())
                    .build());
        }
        t.setStatus(TransferStatus.IN_TRANSIT);
        return mapToResponse(stockTransferRepository.save(t));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public StockTransferResponse receiveTransfer(UUID id) {
        StockTransfer t = stockTransferRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (t.getStatus() != TransferStatus.IN_TRANSIT) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        for (franchiseproject.inventory_service.entity.StockTransferItem item : t.getItems()) {
            Optional<ProductStock> destStockOpt = productStockRepository.findByProductVariantIdAndLocationId(
                    item.getProductVariantId(), t.getToLocationId());
            ProductStock destStock;
            int destBefore = 0;
            if (destStockOpt.isPresent()) {
                destStock = destStockOpt.get();
                destBefore = destStock.getQuantity();
                destStock.setQuantity(destBefore + item.getQuantity());
            } else {
                destStock = ProductStock.builder()
                        .productVariantId(item.getProductVariantId())
                        .locationId(t.getToLocationId())
                        .locationType(t.getType().name().contains("WAREHOUSE") ? "WAREHOUSE" : "FRANCHISE")
                        .quantity(item.getQuantity())
                        .reservedQuantity(0)
                        .minStock(5)
                        .build();
            }
            productStockRepository.save(destStock);

            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .productStock(destStock)
                    .changeQuantity(item.getQuantity())
                    .beforeQuantity(destBefore)
                    .afterQuantity(destStock.getQuantity())
                    .type("TRANSFER_IN")
                    .status("COMPLETED")
                    .referenceType("TRANSFER")
                    .createdBy(t.getCreatedBy())
                    .build());
        }
        t.setStatus(TransferStatus.COMPLETED);
        return mapToResponse(stockTransferRepository.save(t));
    }

    private StockTransferResponse mapToResponse(StockTransfer t) {
        UUID sourceId = t.getFromLocationId();
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
                .items(t.getItems().stream().map(i -> {
                    int currentQty = 0;
                    String pName = "Sản phẩm";
                    try {
                        var apiRes = productClient.getProductVariant(i.getProductVariantId());
                        if (apiRes != null && apiRes.getData() != null) {
                            var detail = apiRes.getData();
                            pName = detail.getProductName() + " - " + (detail.getColor() != null ? detail.getColor() : "N/A") + " - " + (detail.getSize() != null ? detail.getSize() : "N/A");
                        }
                        if (sourceId != null) {
                            var stockOpt = productStockRepository.findByProductVariantIdAndLocationId(i.getProductVariantId(), sourceId);
                            if (stockOpt.isPresent()) {
                                currentQty = stockOpt.get().getQuantity();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Feign/Stock error: " + e.getMessage());
                    }

                    return StockTransferItemResponse.builder()
                            .id(i.getId())
                            .productVariantId(i.getProductVariantId())
                            .productVariantName(pName)
                            .quantity(i.getQuantity())
                            .currentQuantity(currentQty)
                            .build();
                }).collect(Collectors.toList()))
                .build();
    }
}
