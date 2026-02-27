package com.franchiseproject.shiftservice.repository;

import com.franchiseproject.shiftservice.model.StaffShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StaffShiftRepository extends JpaRepository<StaffShift, UUID> {

    List<StaffShift> findByStaffIdAndWorkDate(UUID staffId, LocalDate workDate);

    boolean existsByStaffIdAndWorkDate(UUID staffId, LocalDate workDate);
}
