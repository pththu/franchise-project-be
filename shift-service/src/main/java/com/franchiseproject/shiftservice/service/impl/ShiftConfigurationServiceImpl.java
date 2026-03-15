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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShiftConfigurationServiceImpl implements ShiftConfigurationService {

    private final ShiftConfigurationRepository shiftRepository;
    private final StaffShiftRepository staffShiftRepository;
    private final ShiftMapper shiftMapper;
    private final StaffShiftMapper staffShiftMapper;

    // Constants
    private static final int GRACE_PERIOD_MINUTES = 15;
    private static final int LATE_THRESHOLD_MINUTES = 30;
    private static final int MAX_SHIFTS_PER_WEEK = 6;

    // ================= SHIFT CONFIG =================
    @Override
    public ShiftResponse createShiftConfiguration(CreateShiftRequest request) {
        log.info("Creating shift: {}", request);
        ShiftConfiguration shift = shiftMapper.toEntity(request);
        shift.setId(UUID.randomUUID());
        shift.setStatus(true);
        ShiftResponse response = shiftMapper.toResponse(shiftRepository.save(shift));
        log.info("Shift created: {}", response);
        return response;
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
        // Validate
        ShiftConfiguration shiftConfig = getShiftConfigOrThrow(request.getShiftConfigId());

        if (!shiftConfig.getStatus()) {
            throw new IllegalStateException("Ca làm việc đã bị vô hiệu hóa");
        }

        if (request.getWorkDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể phân ca trong quá khứ");
        }

        if (staffShiftRepository.existsByStaffIdAndWorkDate(request.getStaffId(), request.getWorkDate())) {
            throw new IllegalArgumentException("Nhân viên đã có ca làm việc trong ngày này");
        }

        // Check giới hạn ca/tuần (chỉ tính ca ASSIGNED, CHECKED_IN, CHECKED_OUT)
        checkWeeklyShiftLimit(request.getStaffId(), request.getWorkDate());

        StaffShift staffShift = StaffShift.builder()
                .id(UUID.randomUUID())
                .staffId(request.getStaffId())
                .shiftConfigId(request.getShiftConfigId())
                .workDate(request.getWorkDate())
                .status(ShiftStatus.ASSIGNED)
                .build();

        return staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
    }

    @Override
    public StaffShiftResponse updateAssignedShift(UUID staffShiftId, AssignShiftRequest request) {
        StaffShift staffShift = getStaffShiftOrThrow(staffShiftId);
        boolean hasChanges = false;

        if (request.getWorkDate() != null && !request.getWorkDate().equals(staffShift.getWorkDate())) {
            // Check trùng ngày
            if (staffShiftRepository.existsByStaffIdAndWorkDateAndIdNot(
                    staffShift.getStaffId(), request.getWorkDate(), staffShiftId)) {
                throw new IllegalArgumentException("Nhân viên đã có ca khác trong ngày này");
            }
            staffShift.setWorkDate(request.getWorkDate());
            hasChanges = true;
        }

        if (request.getShiftConfigId() != null && !request.getShiftConfigId().equals(staffShift.getShiftConfigId())) {
            ShiftConfiguration newConfig = shiftRepository.findById(request.getShiftConfigId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca làm việc"));
            if (!newConfig.getStatus()) {
                throw new IllegalStateException("Ca làm việc đã bị vô hiệu hóa");
            }
            staffShift.setShiftConfigId(request.getShiftConfigId());
            hasChanges = true;
        }

        if (!hasChanges) {
            return staffShiftMapper.toResponse(staffShift);
        }

        return staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
    }

    // ================= ATTENDANCE =================
    @Override
    public StaffShiftResponse checkIn(UUID shiftId) {
        StaffShift staffShift = getStaffShiftOrThrow(shiftId);

        // 1. Kiểm tra trạng thái
        if (staffShift.getStatus() != ShiftStatus.ASSIGNED) {
            throw new IllegalStateException("Không thể check-in. Trạng thái hiện tại: " + staffShift.getStatus());
        }

        // 2. Kiểm tra ngày
        LocalDate today = LocalDate.now();
        if (staffShift.getWorkDate().isBefore(today)) {
            throw new IllegalStateException("Không thể check-in cho ca trong quá khứ");
        }
        if (staffShift.getWorkDate().isAfter(today)) {
            throw new IllegalStateException("Không thể check-in cho ca trong tương lai");
        }

        // 3. Xử lý check-in
        LocalTime now = LocalTime.now();
        ShiftConfiguration config = getShiftConfigOrThrow(staffShift.getShiftConfigId());

        // Kiểm tra check-in sớm (cho cả ca thường và ca đêm)
        if (isTooEarly(now, config)) {
            throw new IllegalStateException("Chưa đến giờ check-in. Giờ bắt đầu: " + config.getStartTime());
        }

        // Tính số phút trễ
        if (now.isAfter(config.getStartTime().plusMinutes(GRACE_PERIOD_MINUTES))) {
            long lateMinutes = Duration.between(config.getStartTime(), now).toMinutes();
            staffShift.setLateMinutes((int) lateMinutes);
            staffShift.setNote("Check-in trễ " + lateMinutes + " phút");

            if (lateMinutes > LATE_THRESHOLD_MINUTES) {
                log.info("Staff {} check-in trễ {} phút", staffShift.getStaffId(), lateMinutes);
            }
        }

        staffShift.setCheckInTime(now);
        staffShift.setStatus(ShiftStatus.CHECKED_IN);

        return staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
    }

    @Override
    public StaffShiftResponse checkOut(UUID shiftId) {
        StaffShift staffShift = getStaffShiftOrThrow(shiftId);

        if (staffShift.getStatus() != ShiftStatus.CHECKED_IN) {
            throw new IllegalStateException("Chưa check-in hoặc đã check-out rồi");
        }

        ShiftConfiguration config = getShiftConfigOrThrow(staffShift.getShiftConfigId());
        LocalTime now = LocalTime.now();

        // Kiểm tra nếu check-out quá giờ
        if (now.isAfter(config.getEndTime().plusMinutes(30))) {
            staffShift.setStatus(ShiftStatus.INCOMPLETE);
            staffShift.setNote("Check-out quá trễ (>30 phút sau giờ kết thúc)");
        } else {
            staffShift.setStatus(ShiftStatus.CHECKED_OUT);
        }

        staffShift.setCheckOutTime(now);
        return staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
    }

    @Override
    public StaffShiftResponse markAbsent(UUID shiftId) {
        StaffShift staffShift = getStaffShiftOrThrow(shiftId);

        if (staffShift.getStatus() != ShiftStatus.ASSIGNED) {
            throw new IllegalStateException("Không thể đánh dấu vắng mặt cho ca đã xử lý");
        }

        if (staffShift.getWorkDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Không thể đánh dấu vắng mặt cho ca trong tương lai");
        }

        staffShift.setStatus(ShiftStatus.ABSENT);
        staffShift.setNote("Vắng mặt");

        return staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
    }

    // ================= SCHEDULE =================
    @Override
    public List<StaffShiftResponse> getSchedule(UUID staffId, LocalDate date) {
        List<StaffShift> shifts;
        if (staffId != null) {
            shifts = staffShiftRepository.findByStaffIdAndWorkDate(staffId, date);
        } else {
            shifts = staffShiftRepository.findByWorkDate(date);
        }

        return shifts.stream()
                .map(shift -> {
                    StaffShiftResponse response = staffShiftMapper.toResponse(shift);
                    enhanceWithShiftDetails(response, shift);
                    return response;
                })
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
                .totalIncomplete(countByStatus(shifts, ShiftStatus.INCOMPLETE))
                .build();
    }

    @Override
    public PersonalStatisticResponse getPersonalStatistic(UUID staffId) {
        List<StaffShift> shifts = staffShiftRepository.findByStaffId(staffId);

        return PersonalStatisticResponse.builder()
                .totalShifts(shifts.size())
                .totalCompleted(countByStatus(shifts, ShiftStatus.CHECKED_OUT))
                .totalAbsent(countByStatus(shifts, ShiftStatus.ABSENT))
                .totalLate(countLateShifts(shifts))
                .totalIncomplete(countByStatus(shifts, ShiftStatus.INCOMPLETE))
                .build();
    }

    // ================= ATTENDANCE REPORT =================
    @Override
    public List<StaffShiftResponse> getIncompleteShifts(LocalDate date) {
        return staffShiftRepository.findByWorkDateAndStatus(date, ShiftStatus.INCOMPLETE)
                .stream()
                .map(staffShiftMapper::toResponse)
                .toList();
    }

    @Override
    public Map<String, Object> getAttendanceSummary(LocalDate date) {
        List<StaffShift> shifts = staffShiftRepository.findByWorkDate(date);

        return Map.of(
                "date", date.toString(),
                "totalShifts", shifts.size(),
                "checkedOut", countByStatus(shifts, ShiftStatus.CHECKED_OUT),
                "checkedIn", countByStatus(shifts, ShiftStatus.CHECKED_IN),
                "assigned", countByStatus(shifts, ShiftStatus.ASSIGNED),
                "absent", countByStatus(shifts, ShiftStatus.ABSENT),
                "incomplete", countByStatus(shifts, ShiftStatus.INCOMPLETE),
                "lateList", shifts.stream()
                        .filter(s -> s.getLateMinutes() != null && s.getLateMinutes() > 0)
                        .map(s -> Map.of(
                                "staffId", s.getStaffId().toString(),
                                "lateMinutes", s.getLateMinutes(),
                                "note", s.getNote() != null ? s.getNote() : ""
                        ))
                        .toList()
        );
    }

    // ================= JOB XỬ LÝ QUÊN CHECK-OUT =================
    @Scheduled(cron = "0 0/30 * * * ?", zone = "Asia/Ho_Chi_Minh") // Chạy mỗi 30 phút
    @Transactional
    public void handleMissedCheckOuts() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        log.info("Running missed checkout job at {}", now);

        // Lấy tất cả ca đang CHECKED_IN
        List<StaffShift> activeShifts = staffShiftRepository
                .findByWorkDateAndStatus(today, ShiftStatus.CHECKED_IN);

        for (StaffShift shift : activeShifts) {
            try {
                ShiftConfiguration config = getShiftConfigOrThrow(shift.getShiftConfigId());

                // Kiểm tra nếu đã quá giờ kết thúc 30 phút
                LocalTime endTime = config.getEndTime();

                if (now.isAfter(endTime.plusMinutes(30))) {
                    shift.setStatus(ShiftStatus.INCOMPLETE);
                    shift.setCheckOutTime(endTime); // Ghi nhận giờ kết thúc dự kiến
                    shift.setNote("Quên check-out - tự động xử lý sau 30 phút");

                    staffShiftRepository.save(shift);
                    log.info("Marked shift {} as incomplete (missed checkout after 30 mins)", shift.getId());
                }
            } catch (Exception e) {
                log.error("Failed to process shift {}", shift.getId(), e);
            }
        }
    }

    // ================= PRIVATE HELPERS =================

    private ShiftConfiguration getShiftConfigOrThrow(UUID id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca làm việc"));
    }

    private StaffShift getStaffShiftOrThrow(UUID id) {
        return staffShiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca đã phân công"));
    }

    private void checkWeeklyShiftLimit(UUID staffId, LocalDate workDate) {
        LocalDate weekStart = workDate.minusDays(workDate.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        // Chỉ đếm ca thật (không tính ABSENT, INCOMPLETE)
        long weeklyShifts = staffShiftRepository.countByStaffIdAndWorkDateBetweenAndStatusIn(
                staffId,
                weekStart,
                weekEnd,
                List.of(ShiftStatus.ASSIGNED, ShiftStatus.CHECKED_IN, ShiftStatus.CHECKED_OUT)
        );

        if (weeklyShifts >= MAX_SHIFTS_PER_WEEK) {
            throw new IllegalStateException("Nhân viên đã đạt giới hạn " + MAX_SHIFTS_PER_WEEK + " ca/tuần");
        }
    }

    private boolean isTooEarly(LocalTime now, ShiftConfiguration config) {
        LocalTime startTime = config.getStartTime();
        LocalTime endTime = config.getEndTime();

        // Ca đêm (start > end)
        if (startTime.isAfter(endTime)) {
            // Cho phép check-in từ 1 tiếng trước giờ bắt đầu
            LocalTime earliestCheckIn = startTime.minusHours(1);

            // Nếu earliestCheckIn < 0 (qua ngày hôm trước)
            if (earliestCheckIn.isAfter(startTime)) { // Bị wrap qua ngày mới
                return now.isBefore(LocalTime.MIN) && now.isAfter(startTime);
            }
            return now.isBefore(earliestCheckIn) && now.isAfter(startTime);
        } else {
            // Ca thường
            return now.isBefore(startTime.minusHours(1));
        }
    }

    private long countByStatus(List<StaffShift> shifts, ShiftStatus status) {
        return shifts.stream()
                .filter(s -> s.getStatus() == status)
                .count();
    }

    private long countLateShifts(List<StaffShift> shifts) {
        return shifts.stream()
                .filter(s -> s.getLateMinutes() != null && s.getLateMinutes() > 0)
                .count();
    }

    private void enhanceWithShiftDetails(StaffShiftResponse response, StaffShift shift) {
        shiftRepository.findById(shift.getShiftConfigId())
                .ifPresent(config -> {
                    response.setShiftName(config.getName());
                    response.setShiftStartTime(config.getStartTime());
                    response.setShiftEndTime(config.getEndTime());
                });
    }
}