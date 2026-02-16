package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.model.Franchise;
import franchiseproject.inventory_service.repository.FranchiseRepository;
import franchiseproject.inventory_service.service.FranchiseService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class FranchiseImpl implements FranchiseService {
    FranchiseRepository franchiseRepository;

    @Override
    public List<Franchise> getAll() {
        return franchiseRepository.findAll();
    }
}
