package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.ApiResponse;
import franchiseproject.inventory_service.dto.request.InitialStockRequest;
import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.ProductStockResponse;

import java.util.UUID;

public interface ProductStockService {
    PageResponse<ProductStockResponse> getStocks(Long locationId, boolean lowStock, int page, int size);
    void addInitialStock(InitialStockRequest request);
}
