package com.franchiseproject.customerservice.mapper;

import com.franchiseproject.customerservice.client.FranchiseClient;
import com.franchiseproject.customerservice.client.IdentityClient;
import com.franchiseproject.customerservice.dto.response.CustomerFranchiseResponse;
import com.franchiseproject.customerservice.dto.response.CustomerSummaryResponse;
import com.franchiseproject.customerservice.dto.response.FranchiseResponse;
import com.franchiseproject.customerservice.dto.response.UserResponse;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CustomerFranchiseMapper {

//    @Mapping(target = "user", source = "userId")
    @Mapping(target = "franchise", source = "franchiseId")
    CustomerFranchiseResponse toCustomerFranchiseResponse(
            CustomerFranchise customerFranchise,
            @Context IdentityClient identityClient,
            @Context FranchiseClient franchiseClient
    );

    @Mapping(target = "user", source = "userId")
    @Mapping(target = "purchasedFranchises", ignore = true)
    @Mapping(target = "loyaltyInfo", ignore = true)
    CustomerSummaryResponse toCustomerSummaryResponse(
            CustomerFranchise customerFranchise,
            @Context IdentityClient identityClient,
            @Context FranchiseClient franchiseClient
    );

    default UserResponse mapUser(
            UUID userId,
            @Context IdentityClient identityClient) {
        if (userId == null) {
            System.out.println("return null: "+ userId);
            return null;
        };
        return identityClient.getUserById(userId);
    }

    default FranchiseResponse mapFranchise(
            UUID franchiseId,
            @Context FranchiseClient franchiseClient) {
        if (franchiseId == null) {
            System.out.println("return null: "+ franchiseId);
            return null;
        };
        return franchiseClient.getFranchiseById(franchiseId);
    }

}