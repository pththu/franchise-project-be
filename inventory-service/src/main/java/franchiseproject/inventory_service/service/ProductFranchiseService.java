package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.entity.ProductFranchise;

import java.util.List;
import java.util.UUID;

public interface ProductFranchiseService {
    List<ProductFranchise> getAll();
    List<ProductFranchise> getAllOfFranchise(UUID franchiseId);
}
