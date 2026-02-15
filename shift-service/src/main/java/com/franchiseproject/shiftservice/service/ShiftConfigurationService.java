package com.franchiseproject.shiftservice.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShiftConfigurationService {

    UUID createShiftConfiguration();

    List<UUID> getShiftConfigurationsByFranchise(UUID franchiseId);

    UUID assignShift(UUID staffId, UUID shiftConfigId, LocalDate workDate);

    void checkIn(UUID shiftId);

    void checkOut(UUID shiftId);

    List<UUID> getSchedule(UUID staffId, LocalDate date);

    void markAbsent(UUID shiftId);
}

