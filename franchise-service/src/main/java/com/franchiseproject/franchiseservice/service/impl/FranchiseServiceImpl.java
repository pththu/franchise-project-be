package com.franchiseproject.franchiseservice.service.impl;

import com.franchiseproject.franchiseservice.dto.FranchiseDTO;
import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import com.franchiseproject.franchiseservice.exception.BadRequestException;
import com.franchiseproject.franchiseservice.exception.ResourceNotFoundException;
import com.franchiseproject.franchiseservice.mapper.FranchiseMapper;
import com.franchiseproject.franchiseservice.model.Franchise;
import com.franchiseproject.franchiseservice.repository.FranchiseRepository;
import com.franchiseproject.franchiseservice.service.FranchiseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FranchiseServiceImpl implements FranchiseService {

    private final FranchiseRepository franchiseRepository;
    private final FranchiseMapper franchiseMapper;

    @Override
    public List<FranchiseDTO> getAllFranchises() {
        return franchiseRepository.findAll()
                .stream()
                .map(franchiseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FranchiseDTO getFranchiseById(Long id) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));
        return franchiseMapper.toDTO(franchise);
    }

    @Override
    @Transactional
    public FranchiseDTO createFranchise(FranchiseDTO franchiseDTO) {
        // Kiểm tra tên franchise đã tồn tại chưa
        if (franchiseRepository.existsByName(franchiseDTO.getName())) {
            throw new BadRequestException("Franchise name already exists: " + franchiseDTO.getName());
        }

        Franchise franchise = franchiseMapper.toEntity(franchiseDTO);

        // SỬA: Dùng NEW thay vì new_ (vì enum là NEW)
        if (franchiseDTO.getStatus() == null) {
            franchise.setStatus(FranchiseStatus.NEW); // Mới tạo thì status = NEW
        }

        Franchise savedFranchise = franchiseRepository.save(franchise);
        log.info("New franchise created: {} with id {}", savedFranchise.getName(), savedFranchise.getId());

        return franchiseMapper.toDTO(savedFranchise);
    }

    @Override
    @Transactional
    public FranchiseDTO updateFranchise(Long id, FranchiseDTO franchiseDTO) {
        Franchise existingFranchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));

        // Kiểm tra nếu đổi tên mà tên mới đã tồn tại (trừ chính nó)
        if (!existingFranchise.getName().equals(franchiseDTO.getName())
                && franchiseRepository.existsByName(franchiseDTO.getName())) {
            throw new BadRequestException("Franchise name already exists: " + franchiseDTO.getName());
        }

        existingFranchise.setName(franchiseDTO.getName());
        existingFranchise.setAddress(franchiseDTO.getAddress());
        existingFranchise.setGoogleMapsUrl(franchiseDTO.getGoogleMapsUrl());
        existingFranchise.setPhone(franchiseDTO.getPhone());
        existingFranchise.setEmail(franchiseDTO.getEmail());
        existingFranchise.setOpened(franchiseDTO.getOpened());
        existingFranchise.setClosed(franchiseDTO.getClosed());
        existingFranchise.setAt(franchiseDTO.getAt());

        // Chỉ update status nếu có trong DTO
        if (franchiseDTO.getStatus() != null) {
            existingFranchise.setStatus(franchiseDTO.getStatus());
        }

        Franchise updatedFranchise = franchiseRepository.save(existingFranchise);
        log.info("Franchise updated: {} with id {}", updatedFranchise.getName(), id);

        return franchiseMapper.toDTO(updatedFranchise);
    }

    @Override
    @Transactional
    public void deleteFranchise(Long id) {
        if (!franchiseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Franchise not found with id: " + id);
        }

        franchiseRepository.deleteById(id);
        log.info("Franchise deleted with id: {}", id);
    }

    @Override
    public List<FranchiseDTO> getFranchisesByStatus(FranchiseStatus status) {
        return franchiseRepository.findByStatus(status)
                .stream()
                .map(franchiseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FranchiseDTO> getFranchisesByManagerId(Integer managerId) {
        return franchiseRepository.findAll()
                .stream()
                .map(franchiseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FranchiseDTO updateFranchiseStatus(Long id, FranchiseStatus status) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));

        franchise.setStatus(status);
        Franchise updatedFranchise = franchiseRepository.save(franchise);
        log.info("Franchise {} status updated to: {}", id, status);

        return franchiseMapper.toDTO(updatedFranchise);
    }
}