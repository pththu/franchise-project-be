package com.franchiseproject.shiftservice.repository;

import com.franchiseproject.shiftservice.model.ShiftConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShiftConfigurationRepository extends JpaRepository<ShiftConfiguration, UUID> {
}
