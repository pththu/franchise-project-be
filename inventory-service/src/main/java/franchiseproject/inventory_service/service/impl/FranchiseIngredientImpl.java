package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.model.FranchiseIngredient;
import franchiseproject.inventory_service.repository.FranchiseIngredientRepository;
import franchiseproject.inventory_service.service.FranchiseIngredientService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FranchiseIngredientImpl implements FranchiseIngredientService {
    FranchiseIngredientRepository franchiseIngredientRepository;

    @Override
    public List<FranchiseIngredient> getAll() {
        return franchiseIngredientRepository.findAll();
    }
}
