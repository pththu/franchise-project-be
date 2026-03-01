package com.franchiseproject.customerservice.mapper;

import com.franchiseproject.customerservice.dto.response.CustomerResponse;
import com.franchiseproject.customerservice.dto.response.LoyaltyInfoResponse;
import com.franchiseproject.customerservice.model.Customer;
import com.franchiseproject.customerservice.model.CustomerFranchise;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerResponse toCustomerResponse(Customer customer);

    LoyaltyInfoResponse toLoyaltyInfoResponse(CustomerFranchise customerFranchise);

    List<LoyaltyInfoResponse> toLoyaltyInfoResponseList(List<CustomerFranchise> list);
}