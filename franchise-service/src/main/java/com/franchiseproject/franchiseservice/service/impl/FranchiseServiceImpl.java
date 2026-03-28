package com.franchiseproject.franchiseservice.service.impl;

import com.franchiseproject.franchiseservice.dto.FranchiseDTO;
import com.franchiseproject.franchiseservice.dto.response.CheckFranchiseResponse;
import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import com.franchiseproject.franchiseservice.exception.AppException;
import com.franchiseproject.franchiseservice.exception.BadRequestException;
import com.franchiseproject.franchiseservice.exception.ErrorCode;
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
import java.util.UUID;
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
    public FranchiseDTO getFranchiseById(UUID id) {  // Đã sửa thành UUID
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));
        return franchiseMapper.toDTO(franchise);
    }

    @Override
    @Transactional
    public FranchiseDTO createFranchise(FranchiseDTO franchiseDTO) {
        if (franchiseRepository.existsByName(franchiseDTO.getName())) {
            throw new BadRequestException("Franchise name already exists: " + franchiseDTO.getName());
        }

        Franchise franchise = franchiseMapper.toEntity(franchiseDTO);

        if (franchiseDTO.getStatus() == null) {
            franchise.setStatus(FranchiseStatus.NEW);
        }

        Franchise savedFranchise = franchiseRepository.save(franchise);
        log.info("New franchise created: {} with id {}", savedFranchise.getName(), savedFranchise.getId());

        return franchiseMapper.toDTO(savedFranchise);
    }

    @Override
    @Transactional
    public FranchiseDTO updateFranchise(UUID id, FranchiseDTO franchiseDTO) {  // Đã sửa thành UUID
        Franchise existingFranchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));

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

        if (franchiseDTO.getStatus() != null) {
            existingFranchise.setStatus(franchiseDTO.getStatus());
        }

        Franchise updatedFranchise = franchiseRepository.save(existingFranchise);
        log.info("Franchise updated: {} with id {}", updatedFranchise.getName(), id);

        return franchiseMapper.toDTO(updatedFranchise);
    }

    @Override
    @Transactional
    public void deleteFranchise(UUID id) {  // Đã sửa thành UUID
//        if (!franchiseRepository.existsById(id)) {
//            throw new ResourceNotFoundException("Franchise not found with id: " + id);
//        }
//
//        franchiseRepository.deleteById(id);
        Franchise existingFranchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));

        existingFranchise.setStatus(FranchiseStatus.DELETED);
        franchiseRepository.save(existingFranchise);
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
    public FranchiseDTO updateFranchiseStatus(UUID id, FranchiseStatus status) {  // Đã sửa thành UUID
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));

        franchise.setStatus(status);
        Franchise updatedFranchise = franchiseRepository.save(franchise);
        log.info("Franchise {} status updated to: {}", id, status);

        return franchiseMapper.toDTO(updatedFranchise);
    }

    @Override
    public CheckFranchiseResponse checkFranchiseById(UUID id) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return CheckFranchiseResponse.builder()
                .isExists(franchise == null ? false : true)
                .status(franchise == null ? null : franchise.getStatus())
                .build();
    }
}