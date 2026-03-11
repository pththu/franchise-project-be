package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.*;

import java.util.List;
import java.util.UUID;

public interface InventoryImportService {
    InventoryImportDetailResponse createImport(CreateInventoryImportRequest request);
    List<InventoryImportResponse> getAllImports();
    InventoryImportDetailResponse getImportById(UUID id);
    InventoryImportDetailResponse updateImport(UUID id, UpdateInventoryImportRequest request);
    void deleteImport(UUID id);
    InventoryImportDetailResponse updateStatus(UUID id, UpdateInventoryImportStatusRequest request);
}