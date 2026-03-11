package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.InventoryLogDetailResponse;
import franchiseproject.inventory_service.dto.InventoryLogResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryLogService {
    List<InventoryLogResponse> getAllLogs(UUID franchiseId);
    InventoryLogDetailResponse getLogDetail(UUID id);
}