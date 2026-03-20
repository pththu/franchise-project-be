package com.franchiseproject.loyaltyservice.mapper;

import com.franchiseproject.loyaltyservice.dto.request.DeductPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.EarnPointsResponse;
import com.franchiseproject.loyaltyservice.dto.response.TransactionHistoryResponse;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoyaltyMapper {

    @Mapping(source = "transaction.id", target = "transactionId")
    @Mapping(source = "transaction.customerId", target = "customerId")
    @Mapping(source = "transaction.points", target = "pointsEarned")
    @Mapping(source = "transaction.balanceAfter", target = "currentBalance")
    @Mapping(source = "transaction.createdAt", target = "timestamp")
    @Mapping(source = "tierName", target = "newTierName")
    EarnPointsResponse toEarnPointsResponse(LoyaltyTransaction transaction, String tierName);

    TransactionHistoryResponse toTransactionHistoryResponse(LoyaltyTransaction transaction);
}