package com.franchiseproject.franchiseservice.repository;

import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import com.franchiseproject.franchiseservice.model.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseRepository extends JpaRepository<Franchise, Long> {
    List<Franchise> findByStatus(FranchiseStatus status);
    boolean existsByName(String name);
}