package com.franchiseproject.customerservice.mapper;

import com.franchiseproject.customerservice.dto.response.CustomerFranchiseResponse;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerFranchiseMapper {

    @Mapping(target = "userResponse", ignore = true)
    CustomerFranchiseResponse toCustomerFranchiseResponse(CustomerFranchise customerFranchise);

}