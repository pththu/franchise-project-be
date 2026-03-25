package com.franchiseproject.customerservice.mapper;

import com.franchiseproject.customerservice.dto.response.CustomerFranchiseResponse;
import com.franchiseproject.customerservice.dto.response.CustomerResponse;
import com.franchiseproject.customerservice.dto.response.LoyaltyInfoResponse;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    LoyaltyInfoResponse toLoyaltyInfoResponse(CustomerFranchise customerFranchise);

    List<LoyaltyInfoResponse> toLoyaltyInfoResponseList(List<CustomerFranchise> list);


    @Mapping(target = "userResponse", ignore = true)
    CustomerResponse toCustomerResponse(CustomerFranchise customerFranchise);


}