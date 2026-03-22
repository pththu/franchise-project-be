package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.request.CreateStockRequest;
import franchiseproject.inventory_service.dto.response.StockRequestResponse;

import java.util.List;
import java.util.UUID;

public interface StockRequestService {
    StockRequestResponse createRequest(CreateStockRequest request);
    List<StockRequestResponse> getAllRequests();
    StockRequestResponse getRequestById(UUID id);
    StockRequestResponse approveRequest(UUID id, UUID sourceLocationId, UUID approvedBy);
    StockRequestResponse shipRequest(UUID id);
    StockRequestResponse receiveRequest(UUID id);
    StockRequestResponse rejectRequest(UUID id, String reason);
    List<StockRequestResponse> getRequestsByFranchiseId(UUID franchiseId);
}
