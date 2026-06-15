package com.franchiseproject.orderservice.mapper;

import com.franchiseproject.orderservice.dto.OrderResponse;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {OrderDetailMapper.class})
public interface OrderMapper {
    OrderResponse toOrderResponse(Order o);
}
