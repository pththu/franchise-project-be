package com.franchiseproject.shiftservice.repository;

import com.franchiseproject.shiftservice.model.ShiftConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShiftConfigurationRepository extends JpaRepository<ShiftConfiguration, UUID> {
    List<ShiftConfiguration> findByFranchiseId(UUID franchiseId);

}
