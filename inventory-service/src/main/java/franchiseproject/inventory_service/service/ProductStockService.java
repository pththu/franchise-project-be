package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.ApiResponse;
import franchiseproject.inventory_service.dto.request.InitialStockRequest;
import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.request.StockRequestItemRequest;
import franchiseproject.inventory_service.dto.response.ProductStockResponse;

import java.util.List;
import java.util.UUID;

public interface ProductStockService {
    PageResponse<ProductStockResponse> getStocks(java.util.UUID locationId, boolean lowStock, int page, int size);
    void addInitialStock(InitialStockRequest request);
    List<UUID> getInStockVariantIds(UUID locationId);
    List<UUID> findCapableBranches(List<StockRequestItemRequest> items);

    void reserveStock(List<StockRequestItemRequest> items, UUID locationId);
    void releaseStock(List<StockRequestItemRequest> items, UUID locationId);

    void commitStock(List<StockRequestItemRequest> items, UUID locationId);

    java.util.Map<UUID, Integer> getBulkAvailableStock(List<UUID> variantIds);
}
