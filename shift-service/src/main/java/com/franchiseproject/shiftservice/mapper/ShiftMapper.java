package com.franchiseproject.shiftservice.mapper;

import com.franchiseproject.shiftservice.dto.CreateShiftRequest;
import com.franchiseproject.shiftservice.dto.ShiftDTO;
import com.franchiseproject.shiftservice.dto.ShiftResponse;
import com.franchiseproject.shiftservice.model.ShiftConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShiftMapper {

    ShiftResponse toResponse(ShiftConfiguration entity);

    ShiftConfiguration toEntity(CreateShiftRequest request);
}