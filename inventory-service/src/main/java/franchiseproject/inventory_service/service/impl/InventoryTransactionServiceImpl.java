package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.InventoryTransactionResponse;
import franchiseproject.inventory_service.entity.InventoryTransaction;
import franchiseproject.inventory_service.repository.InventoryTransactionRepository;
import franchiseproject.inventory_service.service.InventoryTransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import franchiseproject.inventory_service.client.ProductClient;
import franchiseproject.inventory_service.dto.response.ProductVariantDetailResponse;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    InventoryTransactionRepository repository;
    ProductClient productClient;

    @Override
    public PageResponse<InventoryTransactionResponse> getTransactions(java.util.UUID locationId, Instant from, Instant to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InventoryTransaction> txPage;

        if (locationId != null) {
            if (from != null && to != null) {
                txPage = repository.findByLocationIdAndDateBetween(locationId, from, to, pageable);
            } else {
                txPage = repository.findByLocationId(locationId, pageable);
            }
        } else {
            if (from != null && to != null) {
                txPage = repository.findAllByCreatedAtBetween(from, to, pageable);
            } else {
                txPage = repository.findAll(pageable);
            }
        }

        List<InventoryTransactionResponse> responses = txPage.getContent().stream()
                .map(tx -> InventoryTransactionResponse.builder()
                        .id(tx.getId())
                        .productVariantId(tx.getProductStock().getProductVariantId())
                        .locationId(tx.getProductStock().getLocationId())
                        .changeQuantity(tx.getChangeQuantity())
                        .beforeQuantity(tx.getBeforeQuantity())
                        .afterQuantity(tx.getAfterQuantity())
                        .type(tx.getType())
                        .referenceId(tx.getReferenceId())
                        .referenceType(tx.getReferenceType())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // Enrich with product details via Bulk Feign
        List<UUID> variantIds = responses.stream()
                .map(InventoryTransactionResponse::getProductVariantId)
                .distinct()
                .collect(Collectors.toList());

        try {
            var apiRes = productClient.getProductVariantsBulk(variantIds);
            if (apiRes != null && apiRes.getData() != null) {
                java.util.Map<UUID, ProductVariantDetailResponse> detailMap = apiRes.getData().stream()
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
            System.err.println("Feign bulk error mapping transactions: " + e.getMessage());
        }

        return PageResponse.<InventoryTransactionResponse>builder()
                .content(responses)
                .pageNo(txPage.getNumber())
                .pageSize(txPage.getSize())
                .totalElements(txPage.getTotalElements())
                .totalPages(txPage.getTotalPages())
                .last(txPage.isLast())
                .build();
    }
}
