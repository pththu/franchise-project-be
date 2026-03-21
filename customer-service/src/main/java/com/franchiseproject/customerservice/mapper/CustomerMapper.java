package com.franchiseproject.customerservice.mapper;

import com.franchiseproject.customerservice.dto.response.CustomerFranchiseResponse;
import com.franchiseproject.customerservice.dto.response.LoyaltyInfoResponse;
import com.franchiseproject.customerservice.entity.Customer;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerFranchiseResponse toCustomerResponse(CustomerFranchise customerFranchise);

    LoyaltyInfoResponse toLoyaltyInfoResponse(CustomerFranchise customerFranchise);

    List<LoyaltyInfoResponse> toLoyaltyInfoResponseList(List<CustomerFranchise> list);


}