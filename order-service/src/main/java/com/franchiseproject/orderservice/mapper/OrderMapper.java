package com.franchiseproject.orderservice.mapper;

import com.franchiseproject.orderservice.dto.response.OrderByCustomerResponse;
import com.franchiseproject.orderservice.model.Order;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
         uses = {OrderDetailMapper.class}
)
public interface OrderMapper {
    OrderByCustomerResponse toOrderByCustomerResponse(Order order);
}
