package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.dto.ApiResponse;
import franchiseproject.inventory_service.dto.request.InitialStockRequest;
import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.ProductStockResponse;
import franchiseproject.inventory_service.dto.response.ProductVariantDetailResponse;
import franchiseproject.inventory_service.entity.InventoryTransaction;
import franchiseproject.inventory_service.entity.ProductStock;
import franchiseproject.inventory_service.mapper.ProductStockMapper;
import franchiseproject.inventory_service.repository.InventoryTransactionRepository;
import franchiseproject.inventory_service.repository.ProductStockRepository;
import franchiseproject.inventory_service.service.ProductStockService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import franchiseproject.inventory_service.client.ProductClient;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductStockServiceImpl implements ProductStockService {

    ProductStockRepository productStockRepository;
    InventoryTransactionRepository inventoryTransactionRepository;
    ProductStockMapper productStockMapper;
    ProductClient productClient;

    private static final java.util.UUID SYSTEM_WAREHOUSE_ID = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    public PageResponse<ProductStockResponse> getStocks(java.util.UUID locationId, boolean lowStock, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductStock> productStockPage;

        if (lowStock) {
            if (locationId != null) {
                productStockPage = productStockRepository.findLowStockByLocation(locationId, pageable);
            } else {
                productStockPage = productStockRepository.findLowStock(pageable);
            }
        } else {
            if (locationId != null) {
                productStockPage = productStockRepository.findByLocationId(locationId, pageable);
            } else {
                productStockPage = productStockRepository.findAll(pageable);
            }
        }

        List<ProductStockResponse> responses = productStockPage.getContent().stream()
                .map(productStockMapper::toResponse)
                .collect(Collectors.toList());

        // Enrich with product details via Bulk Feign
        java.util.List<java.util.UUID> variantIds = responses.stream()
                .map(ProductStockResponse::getProductVariantId)
                .collect(java.util.stream.Collectors.toList());

        try {
            var apiRes = productClient.getProductVariantsBulk(variantIds);
            if (apiRes != null && apiRes.getData() != null) {
                java.util.Map<java.util.UUID, ProductVariantDetailResponse> detailMap = apiRes.getData().stream()
                        .collect(java.util.stream.Collectors.toMap(ProductVariantDetailResponse::getId, d -> d));
                
                responses.forEach(res -> {
                    var detail = detailMap.get(res.getProductVariantId());
                    if (detail != null) {
                        res.setProductName(detail.getProductName());
                        res.setSize(detail.getSize() != null ? detail.getSize() : "N/A");
                        res.setColor(detail.getColor() != null ? detail.getColor() : "N/A");
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Feign bulk error picking variant details: " + e.getMessage());
        }


        return PageResponse.<ProductStockResponse>builder()
                .content(responses)
                .pageNo(productStockPage.getNumber())
                .pageSize(productStockPage.getSize())
                .totalElements(productStockPage.getTotalElements())
                .totalPages(productStockPage.getTotalPages())
                .last(productStockPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void addInitialStock(InitialStockRequest request) {
        java.util.UUID targetLocationId = request.getLocationId() != null ? request.getLocationId() : SYSTEM_WAREHOUSE_ID;
        String locationType = request.getLocationId() != null ? "FRANCHISE" : "WAREHOUSE";

        Optional<ProductStock> existingStockOpt = productStockRepository.findByProductVariantIdAndLocationId(
                request.getProductVariantId(), targetLocationId);

        ProductStock stock;
        int beforeQty = 0;

        if (existingStockOpt.isPresent()) {
            stock = existingStockOpt.get();
            beforeQty = stock.getQuantity();
            stock.setQuantity(beforeQty + request.getQuantity());
        } else {
            stock = ProductStock.builder()
                    .productVariantId(request.getProductVariantId())
                    .locationId(targetLocationId)
                    .locationType(locationType)
                    .quantity(request.getQuantity())
                    .reservedQuantity(0)
                    .minStock(5) // Default safety stock
                    .build();
        }

        ProductStock savedStock = productStockRepository.save(stock);

        // Record Transaction
        InventoryTransaction tx = InventoryTransaction.builder()
                .productStock(savedStock)
                .changeQuantity(request.getQuantity())
                .beforeQuantity(beforeQty)
                .afterQuantity(savedStock.getQuantity())
                .type("IMPORT") // Standard Type for inbound stock
                .status("COMPLETED")
                .referenceType("IMPORT")
                .createdBy(request.getCreatedBy())
                .build();

        inventoryTransactionRepository.save(tx);
    }
}
