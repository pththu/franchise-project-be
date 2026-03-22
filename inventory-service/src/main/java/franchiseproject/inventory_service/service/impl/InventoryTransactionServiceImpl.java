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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    InventoryTransactionRepository repository;

    @Override
    public PageResponse<InventoryTransactionResponse> getTransactions(Instant from, Instant to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InventoryTransaction> txPage;

        if (from != null && to != null) {
            txPage = repository.findAllByCreatedAtBetween(from, to, pageable);
        } else {
            txPage = repository.findAll(pageable);
        }

        return PageResponse.<InventoryTransactionResponse>builder()
                .content(txPage.getContent().stream()
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
                        .collect(Collectors.toList()))
                .pageNo(txPage.getNumber())
                .pageSize(txPage.getSize())
                .totalElements(txPage.getTotalElements())
                .totalPages(txPage.getTotalPages())
                .last(txPage.isLast())
                .build();
    }
}
