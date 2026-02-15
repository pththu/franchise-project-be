package com.franchiseproject.shiftservice.service.impl;

import com.franchiseproject.shiftservice.model.ShiftConfiguration;
import com.franchiseproject.shiftservice.model.StaffShift;
import com.franchiseproject.shiftservice.repository.ShiftConfigurationRepository;
import com.franchiseproject.shiftservice.repository.StaffShiftRepository;
import com.franchiseproject.shiftservice.service.ShiftConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftConfigurationServiceImpl implements ShiftConfigurationService {

    private final ShiftConfigurationRepository shiftRepository;
    private final StaffShiftRepository staffShiftRepository;

    @Override
    public UUID createShiftConfiguration() {

        ShiftConfiguration shift = ShiftConfiguration.builder()
                .id(UUID.randomUUID())
                .franchiseId(UUID.randomUUID())
                .name("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .breakMinutes(60)
                .status(true)
                .build();

        shiftRepository.save(shift);
        return shift.getId();
    }

    @Override
    public List<UUID> getShiftConfigurationsByFranchise(UUID franchiseId) {
        return shiftRepository.findByFranchiseId(franchiseId)
                .stream()
                .map(ShiftConfiguration::getId)
                .toList();
    }

    @Override
    public UUID assignShift(UUID staffId, UUID shiftConfigId, LocalDate workDate) {

        StaffShift staffShift = StaffShift.builder()
                .id(UUID.randomUUID())
                .staffId(staffId)
                .shiftConfigId(shiftConfigId)
                .workDate(workDate)
                .status("ASSIGNED")
                .build();

        staffShiftRepository.save(staffShift);
        return staffShift.getId();
    }

    @Override
    public void checkIn(UUID shiftId) {

        StaffShift staffShift = staffShiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        staffShift.setCheckInTime(LocalTime.now());
        staffShift.setStatus("CHECKED_IN");

        staffShiftRepository.save(staffShift);
    }

    @Override
    public void checkOut(UUID shiftId) {

        StaffShift staffShift = staffShiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        staffShift.setCheckOutTime(LocalTime.now());
        staffShift.setStatus("CHECKED_OUT");

        staffShiftRepository.save(staffShift);
    }

    @Override
    public List<UUID> getSchedule(UUID staffId, LocalDate date) {

        return staffShiftRepository
                .findByStaffIdAndWorkDate(staffId, date)
                .stream()
                .map(StaffShift::getShiftConfigId)
                .toList();
    }

    @Override
    public void markAbsent(UUID shiftId) {

        StaffShift staffShift = staffShiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        staffShift.setStatus("ABSENT");

        staffShiftRepository.save(staffShift);
    }
}

