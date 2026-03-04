package com.franchiseproject.shiftservice.service.impl;

import com.franchiseproject.shiftservice.dto.request.AssignShiftRequest;
import com.franchiseproject.shiftservice.dto.request.CreateShiftRequest;
import com.franchiseproject.shiftservice.dto.response.PersonalStatisticResponse;
import com.franchiseproject.shiftservice.dto.response.ShiftResponse;
import com.franchiseproject.shiftservice.dto.response.ShiftStatisticResponse;
import com.franchiseproject.shiftservice.dto.response.StaffShiftResponse;
import com.franchiseproject.shiftservice.mapper.ShiftMapper;
import com.franchiseproject.shiftservice.mapper.StaffShiftMapper;
import com.franchiseproject.shiftservice.model.ShiftConfiguration;
import com.franchiseproject.shiftservice.enums.ShiftStatus;
import com.franchiseproject.shiftservice.model.StaffShift;
import com.franchiseproject.shiftservice.repository.ShiftConfigurationRepository;
import com.franchiseproject.shiftservice.repository.StaffShiftRepository;
import com.franchiseproject.shiftservice.service.ShiftConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftConfigurationServiceImpl implements ShiftConfigurationService {

    private final ShiftConfigurationRepository shiftRepository;
    private final StaffShiftRepository staffShiftRepository;
    private final ShiftMapper shiftMapper;
    private final StaffShiftMapper staffShiftMapper;

    // ================= SHIFT CONFIG =================

    @Override
    public ShiftResponse createShiftConfiguration(CreateShiftRequest request) {

        ShiftConfiguration shift = shiftMapper.toEntity(request);
        shift.setId(UUID.randomUUID());
        shift.setStatus(true);

        return shiftMapper.toResponse(shiftRepository.save(shift));
    }

    @Override
    public ShiftResponse updateShiftConfiguration(UUID id, CreateShiftRequest request) {

        ShiftConfiguration shift = getShiftConfigOrThrow(id);

        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setFranchiseId(request.getFranchiseId());

        return shiftMapper.toResponse(shiftRepository.save(shift));
    }

    @Override
    public ShiftResponse deleteShiftConfiguration(UUID id) {

        ShiftConfiguration shift = getShiftConfigOrThrow(id);
        shiftRepository.delete(shift);

        return shiftMapper.toResponse(shift);
    }

    @Override
    public List<ShiftResponse> getShiftConfigurationsByFranchise(UUID franchiseId) {
        return shiftRepository.findByFranchiseId(franchiseId)
                .stream()
                .map(shiftMapper::toResponse)
                .toList();
    }

    // ================= ASSIGN SHIFT =================

    @Override
    public StaffShiftResponse assignShift(AssignShiftRequest request) {

        ShiftConfiguration shiftConfig = getShiftConfigOrThrow(request.getShiftConfigId());

        if (!shiftConfig.getStatus()) {
            throw new IllegalStateException("Shift configuration is inactive");
        }

        if (request.getWorkDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot assign shift in the past");
        }

        if (staffShiftRepository.existsByStaffIdAndWorkDate(
                request.getStaffId(),
                request.getWorkDate())) {

            throw new IllegalArgumentException("Staff already has a shift on this date");
        }

        StaffShift staffShift = StaffShift.builder()
                .id(UUID.randomUUID())
                .staffId(request.getStaffId())
                .shiftConfigId(request.getShiftConfigId())
                .workDate(request.getWorkDate())
                .status(ShiftStatus.ASSIGNED)
                .build();

        return staffShiftMapper.toResponse(
                staffShiftRepository.save(staffShift)
        );
    }

    @Override
    public StaffShiftResponse updateAssignedShift(UUID staffShiftId, AssignShiftRequest request) {
        StaffShift staffShift = getStaffShiftOrThrow(staffShiftId);

        boolean hasChanges = false;

        // Chỉ update workDate nếu được gửi
        if (request.getWorkDate() != null) {
            staffShift.setWorkDate(request.getWorkDate());
            hasChanges = true;

            // Check trùng ngày chỉ khi workDate thay đổi
            if (staffShiftRepository.existsByStaffIdAndWorkDateAndIdNot(
                    staffShift.getStaffId(),
                    request.getWorkDate(),
                    staffShiftId)) {
                throw new IllegalArgumentException("Staff already has another shift on this date");
            }
        }

        // Chỉ update shiftConfigId nếu được gửi (và hợp lệ)
        if (request.getShiftConfigId() != null) {
            ShiftConfiguration newConfig = shiftRepository.findById(request.getShiftConfigId())
                    .orElseThrow(() -> new IllegalArgumentException("Shift configuration not found"));

            if (!newConfig.getStatus()) {
                throw new IllegalStateException("Shift configuration is inactive");
            }

            staffShift.setShiftConfigId(request.getShiftConfigId());
            hasChanges = true;
        }

        // Nếu không có thay đổi gì thì trả về hiện tại (hoặc throw nếu muốn strict)
        if (!hasChanges) {
            return staffShiftMapper.toResponse(staffShift);  // hoặc throw nếu business yêu cầu phải có thay đổi
        }

        return staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
    }

    // ================= ATTENDANCE =================

    @Override
    public StaffShiftResponse checkIn(UUID shiftId) {

        StaffShift staffShift = getStaffShiftOrThrow(shiftId);

        if (staffShift.getStatus() != ShiftStatus.ASSIGNED) {
            throw new IllegalStateException("Cannot check in. Invalid shift status.");
        }

        staffShift.setCheckInTime(LocalTime.now());
        staffShift.setStatus(ShiftStatus.CHECKED_IN);

        return staffShiftMapper.toResponse(staffShift);
    }

    @Override
    public StaffShiftResponse checkOut(UUID shiftId) {

        StaffShift staffShift = getStaffShiftOrThrow(shiftId);

        if (staffShift.getStatus() != ShiftStatus.CHECKED_IN) {
            throw new IllegalStateException("Cannot check out before check in.");
        }

        staffShift.setCheckOutTime(LocalTime.now());
        staffShift.setStatus(ShiftStatus.CHECKED_OUT);

        return staffShiftMapper.toResponse(staffShift);
    }

    @Override
    public StaffShiftResponse markAbsent(UUID shiftId) {

        StaffShift staffShift = getStaffShiftOrThrow(shiftId);

        if (staffShift.getStatus() != ShiftStatus.ASSIGNED) {
            throw new IllegalStateException("Cannot mark absent");
        }

        staffShift.setStatus(ShiftStatus.ABSENT);

        return staffShiftMapper.toResponse(staffShift);
    }

    // ================= SCHEDULE =================

    @Override
    public List<StaffShiftResponse> getSchedule(UUID staffId, LocalDate date) {
        return staffShiftRepository.findByStaffIdAndWorkDate(staffId, date)
                .stream()
                .map(staffShiftMapper::toResponse)
                .toList();
    }

    @Override
    public List<StaffShiftResponse> getScheduleByDate(LocalDate date) {
        return staffShiftRepository.findByWorkDate(date)
                .stream()
                .map(staffShiftMapper::toResponse)
                .toList();
    }

    // ================= STATISTICS =================

    @Override
    public ShiftStatisticResponse getStatisticByDate(LocalDate date) {

        List<StaffShift> shifts = staffShiftRepository.findByWorkDate(date);

        return ShiftStatisticResponse.builder()
                .totalShifts(shifts.size())
                .totalAssigned(countByStatus(shifts, ShiftStatus.ASSIGNED))
                .totalCheckedIn(countByStatus(shifts, ShiftStatus.CHECKED_IN))
                .totalCheckedOut(countByStatus(shifts, ShiftStatus.CHECKED_OUT))
                .totalAbsent(countByStatus(shifts, ShiftStatus.ABSENT))
                .build();
    }

    @Override
    public PersonalStatisticResponse getPersonalStatistic(UUID staffId) {

        List<StaffShift> shifts = staffShiftRepository.findByStaffId(staffId);

        return PersonalStatisticResponse.builder()
                .totalShifts(shifts.size())
                .totalCompleted(countByStatus(shifts, ShiftStatus.CHECKED_OUT))
                .totalAbsent(countByStatus(shifts, ShiftStatus.ABSENT))
                .build();
    }


    // ================= PRIVATE HELPERS =================

    private ShiftConfiguration getShiftConfigOrThrow(UUID id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift configuration not found"));
    }

    private StaffShift getStaffShiftOrThrow(UUID id) {
        return staffShiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff shift not found"));
    }

    private long countByStatus(List<StaffShift> shifts, ShiftStatus status) {
        return shifts.stream()
                .filter(s -> s.getStatus() == status)
                .count();
    }
}