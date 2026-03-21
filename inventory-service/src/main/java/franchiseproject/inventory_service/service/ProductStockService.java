package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.ProductStockResponse;

import java.util.UUID;

public interface ProductStockService {
    PageResponse<ProductStockResponse> getStocks(UUID locationId, boolean lowStock, int page, int size);
}
