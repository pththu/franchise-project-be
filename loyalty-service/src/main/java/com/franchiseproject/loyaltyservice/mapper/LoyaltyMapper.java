package com.franchiseproject.loyaltyservice.mapper;

import com.franchiseproject.loyaltyservice.dto.response.AdjustPointsResponse;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoyaltyMapper {

    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "points", target = "pointsAdjusted")
    @Mapping(source = "createdAt", target = "timestamp")
    AdjustPointsResponse toAdjustResponse(LoyaltyTransaction transaction);
}