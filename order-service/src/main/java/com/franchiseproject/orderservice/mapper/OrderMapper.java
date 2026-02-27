package com.franchiseproject.orderservice.mapper;

import com.franchiseproject.orderservice.dto.response.OrderResponse;
import com.franchiseproject.orderservice.model.Order;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
         uses = {OrderDetailMapper.class}
)
public interface OrderMapper {
    OrderResponse toOrderResponse(Order order);
}
