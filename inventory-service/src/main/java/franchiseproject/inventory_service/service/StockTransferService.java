package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.request.CreateStockTransferRequest;
import franchiseproject.inventory_service.dto.response.StockTransferResponse;

import java.util.UUID;

public interface StockTransferService {
    StockTransferResponse createTransfer(CreateStockTransferRequest request);
    PageResponse<StockTransferResponse> getAllTransfers(int page, int size, UUID fromLocationId);
    StockTransferResponse getTransferById(UUID id);
    StockTransferResponse shipTransfer(UUID id);
    StockTransferResponse receiveTransfer(UUID id);
}
