package com.franchiseproject.shiftservice.repository;

import com.franchiseproject.shiftservice.enums.ShiftStatus;
import com.franchiseproject.shiftservice.model.StaffShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface StaffShiftRepository extends JpaRepository<StaffShift, UUID> {

    List<StaffShift> findByStaffIdAndWorkDate(UUID staffId, LocalDate workDate);

    boolean existsByStaffIdAndWorkDate(UUID staffId, LocalDate workDate);

    boolean existsByStaffIdAndWorkDateAndIdNot(UUID staffId, LocalDate workDate, UUID staffShiftId);

    List<StaffShift> findByWorkDate(LocalDate workDate);

    List<StaffShift> findByStaffId(UUID staffId);

    List<StaffShift> findByWorkDateAndStatus(LocalDate date, ShiftStatus status);

    long countByStaffIdAndWorkDateBetween(UUID staffId, LocalDate start, LocalDate end);

    long countByStaffIdAndWorkDateBetweenAndStatusIn(UUID staffId, LocalDate weekStart, LocalDate weekEnd, List<ShiftStatus> assigned);
}
