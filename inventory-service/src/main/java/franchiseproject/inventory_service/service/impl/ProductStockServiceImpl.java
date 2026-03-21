package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.ProductStockResponse;
import franchiseproject.inventory_service.entity.ProductStock;
import franchiseproject.inventory_service.mapper.ProductStockMapper;
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

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductStockServiceImpl implements ProductStockService {

    ProductStockRepository productStockRepository;
    ProductStockMapper productStockMapper;

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

        return PageResponse.<ProductStockResponse>builder()
                .content(productStockPage.getContent().stream()
                        .map(productStockMapper::toResponse)
                        .collect(Collectors.toList()))
                .pageNo(productStockPage.getNumber())
                .pageSize(productStockPage.getSize())
                .totalElements(productStockPage.getTotalElements())
                .totalPages(productStockPage.getTotalPages())
                .last(productStockPage.isLast())
                .build();
    }
}
