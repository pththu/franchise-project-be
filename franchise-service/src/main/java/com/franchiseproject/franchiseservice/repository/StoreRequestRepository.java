package com.franchiseproject.franchiseservice.repository;

import com.franchiseproject.franchiseservice.enums.RequestStatus;
import com.franchiseproject.franchiseservice.model.Franchise;
import com.franchiseproject.franchiseservice.model.StoreRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRequestRepository extends JpaRepository<StoreRequest, UUID> {  // Changed Long to UUID

    List<StoreRequest> findByFranchise(Franchise franchise);

    List<StoreRequest> findByStatus(RequestStatus status);

    List<StoreRequest> findByCreatedBy(UUID createdBy);  // Changed from String to UUID

    Optional<StoreRequest> findByRequestCode(String requestCode);

    @Query("SELECT sr FROM StoreRequest sr WHERE sr.createdBy = :createdBy AND sr.status = :status")
    List<StoreRequest> findByCreatedByAndStatus(@Param("createdBy") UUID createdBy,
                                                @Param("status") RequestStatus status);

    @Query("SELECT sr FROM StoreRequest sr WHERE sr.franchise.id = :franchiseId AND sr.status = :status")
    List<StoreRequest> findByFranchiseIdAndStatus(@Param("franchiseId") UUID franchiseId,
                                                  @Param("status") RequestStatus status);

    @Query("SELECT sr FROM StoreRequest sr WHERE DATE(sr.createdAt) = :date")
    List<StoreRequest> findByCreatedDate(@Param("date") LocalDate date);

    boolean existsByRequestCode(String requestCode);
}