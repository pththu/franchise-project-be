package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.dto.request.InitialStockRequest;
import franchiseproject.inventory_service.dto.request.StockRequestItemRequest;
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
import java.util.Map;
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

    private static final UUID SYSTEM_WAREHOUSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    public PageResponse<ProductStockResponse> getStocks(UUID locationId, boolean lowStock, int page, int size) {
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
        List<UUID> variantIds = responses.stream()
                .map(ProductStockResponse::getProductVariantId)
                .collect(Collectors.toList());

        try {
            var apiRes = productClient.getProductVariantsBulk(variantIds);
            if (apiRes != null && apiRes.getData() != null) {
                Map<UUID, ProductVariantDetailResponse> detailMap = apiRes.getData().stream()
                        .collect(Collectors.toMap(ProductVariantDetailResponse::getId, d -> d));
                
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
        UUID targetLocationId = request.getLocationId() != null ? request.getLocationId() : SYSTEM_WAREHOUSE_ID;
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

    @Override
    public List<UUID> getInStockVariantIds(UUID locationId) {
        if (locationId == null) {
            return List.of();
        }
        return productStockRepository.findInStockVariantIds(locationId);
    }

    @Override
    public List<UUID> findCapableBranches(List<StockRequestItemRequest> items) {
        if (items == null || items.isEmpty()) return List.of();

        List<UUID> alternativeLocationIds = new java.util.ArrayList<>();
        List<UUID> variantIds = items.stream().map(StockRequestItemRequest::getProductVariantId).collect(Collectors.toList());
        List<ProductStock> stocks = productStockRepository.findByProductVariantIdIn(variantIds);

        Map<UUID, List<ProductStock>> locationStocks = stocks.stream()
                .collect(Collectors.groupingBy(ProductStock::getLocationId));

        for (Map.Entry<UUID, List<ProductStock>> entry : locationStocks.entrySet()) {
            UUID locId = entry.getKey();
            List<ProductStock> locStockList = entry.getValue();
            boolean allMatch = true;

            for (StockRequestItemRequest item : items) {
                Optional<ProductStock> match = locStockList.stream()
                        .filter(s -> s.getProductVariantId().equals(item.getProductVariantId()))
                        .findFirst();
                if (match.isEmpty() || (match.get().getQuantity() - match.get().getReservedQuantity()) < item.getQuantity()) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                alternativeLocationIds.add(locId);
            }
        }
        return alternativeLocationIds;
    }

    @Override
    @Transactional
    public void reserveStock(List<StockRequestItemRequest> items, java.util.UUID locationId) {
        for (StockRequestItemRequest item : items) {
            ProductStock stock = productStockRepository.findByProductVariantIdAndLocationId(
                    item.getProductVariantId(), locationId)
                    .orElseThrow(() -> new RuntimeException("Stock not found for variant " + item.getProductVariantId()));

            if ((stock.getQuantity() - stock.getReservedQuantity()) < item.getQuantity()) {
                throw new RuntimeException("Insufficient available stock for variant " + item.getProductVariantId());
            }

            stock.setReservedQuantity(stock.getReservedQuantity() + item.getQuantity());
            productStockRepository.save(stock);
        }
    }

    @Override
    @Transactional
    public void releaseStock(List<StockRequestItemRequest> items, java.util.UUID locationId) {
        for (StockRequestItemRequest item : items) {
            ProductStock stock = productStockRepository.findByProductVariantIdAndLocationId(
                    item.getProductVariantId(), locationId)
                    .orElseThrow(() -> new RuntimeException("Stock not found for variant " + item.getProductVariantId()));

            if (stock.getReservedQuantity() < item.getQuantity()) {
                System.err.println("Warning: Insufficient reserved stock to release for variant " + item.getProductVariantId());
                continue;
            }

            stock.setReservedQuantity(stock.getReservedQuantity() - item.getQuantity());
            productStockRepository.save(stock);
        }
    }

    @Override
    @Transactional
    public void commitStock(List<StockRequestItemRequest> items, java.util.UUID locationId) {
        for (StockRequestItemRequest item : items) {
            ProductStock stock = productStockRepository.findByProductVariantIdAndLocationId(
                    item.getProductVariantId(), locationId)
                    .orElseThrow(() -> new RuntimeException("Stock not found for variant " + item.getProductVariantId()));

            if (stock.getReservedQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient reserved stock for variant " + item.getProductVariantId());
            }

            int beforeQty = stock.getQuantity(); // Physical stock before deduction
            stock.setQuantity(stock.getQuantity() - item.getQuantity());
            stock.setReservedQuantity(stock.getReservedQuantity() - item.getQuantity());
            ProductStock savedStock = productStockRepository.save(stock);

            InventoryTransaction tx = InventoryTransaction.builder()
                    .productStock(savedStock)
                    .changeQuantity(-item.getQuantity())
                    .beforeQuantity(beforeQty)
                    .afterQuantity(savedStock.getQuantity())
                    .type("SALE")
                    .status("COMPLETED")
                    .referenceType("ORDER")
                    .build();
            inventoryTransactionRepository.save(tx);
        }
    }

    @Override
    public Map<UUID, Integer> getBulkAvailableStock(List<UUID> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return Map.of();
        
        List<ProductStock> stocks = productStockRepository.findByProductVariantIdIn(variantIds);
        Map<UUID, Integer> availableStockMap = new java.util.HashMap<>();
        
        for (UUID vid : variantIds) {
            int totalQty = stocks.stream()
                    .filter(s -> s.getProductVariantId().equals(vid))
                    .mapToInt(s -> s.getQuantity() - s.getReservedQuantity())
                    .sum();
            availableStockMap.put(vid, Math.max(0, totalQty));
        }
        return availableStockMap;
    }
}
