package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.CreateFranchiseRequest;
import franchiseproject.inventory_service.dto.UpdateFranchiseRequest;
import franchiseproject.inventory_service.model.Franchise;

import java.util.List;
import java.util.UUID;

public interface FranchiseService {
    Franchise create(CreateFranchiseRequest request);

    List<Franchise> getAll();

    Franchise getById(UUID id);

    Franchise update(UUID id, UpdateFranchiseRequest request);

    void delete(UUID id);

    Franchise updateStatus(UUID id, Boolean isActive);

}
