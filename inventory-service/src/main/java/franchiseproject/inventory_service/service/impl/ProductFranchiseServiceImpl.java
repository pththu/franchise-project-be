package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.entity.ProductFranchise;
import franchiseproject.inventory_service.repository.ProductFranchiseRepository;
import franchiseproject.inventory_service.service.ProductFranchiseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductFranchiseServiceImpl implements ProductFranchiseService {

    ProductFranchiseRepository productFranchiseRepository;

    @Override
    public List<ProductFranchise> getAll() {
        return productFranchiseRepository.findAll();
    }

    @Override
    public List<ProductFranchise> getAllOfFranchise(UUID franchiseId) {
        return productFranchiseRepository.findByFranchiseId(franchiseId);
    }
}
