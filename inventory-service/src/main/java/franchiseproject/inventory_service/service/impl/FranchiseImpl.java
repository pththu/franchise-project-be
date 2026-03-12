package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.dto.CreateFranchiseRequest;
import franchiseproject.inventory_service.dto.UpdateFranchiseRequest;
import franchiseproject.inventory_service.exception.ResourceNotFoundException;
import franchiseproject.inventory_service.model.Franchise;
import franchiseproject.inventory_service.repository.FranchiseRepository;
import franchiseproject.inventory_service.service.FranchiseService;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FranchiseImpl implements FranchiseService {

    FranchiseRepository franchiseRepository;

    @Override
    public Franchise create(CreateFranchiseRequest request) {

        Franchise franchise = Franchise.builder()
                .name(request.getName())
                .address(request.getAddress())
                .openedAt(request.getOpenedAt())
                .closedAt(request.getClosedAt())
                .isActive(true)
                .build();

        return franchiseRepository.save(franchise);
    }

    @Override
    public List<Franchise> getAll() {
        return franchiseRepository.findAll();
    }

    @Override
    public Franchise getById(UUID id) {
        return franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found"));
    }

    @Override
    public Franchise update(UUID id, UpdateFranchiseRequest request) {

        Franchise franchise = getById(id);

        franchise.setName(request.getName());
        franchise.setAddress(request.getAddress());
        franchise.setOpenedAt(request.getOpenedAt());
        franchise.setClosedAt(request.getClosedAt());

        return franchiseRepository.save(franchise);
    }

    @Override
    public void delete(UUID id) {

        Franchise franchise = getById(id);
        franchiseRepository.delete(franchise);
    }

    @Override
    public Franchise updateStatus(UUID id, Boolean isActive) {

        Franchise franchise = getById(id);

        franchise.setIsActive(isActive);

        return franchiseRepository.save(franchise);
    }
}