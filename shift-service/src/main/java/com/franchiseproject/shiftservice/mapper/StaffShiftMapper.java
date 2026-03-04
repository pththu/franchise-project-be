package com.franchiseproject.shiftservice.mapper;

import com.franchiseproject.shiftservice.dto.response.StaffShiftResponse;
import com.franchiseproject.shiftservice.model.StaffShift;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StaffShiftMapper {

    StaffShiftResponse toResponse(StaffShift entity);
}
