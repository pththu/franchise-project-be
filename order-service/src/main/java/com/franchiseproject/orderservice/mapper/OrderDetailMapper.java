package com.franchiseproject.orderservice.mapper;

import com.franchiseproject.orderservice.dto.OrderItemResponse;
import com.franchiseproject.orderservice.entity.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring"
)
public interface OrderDetailMapper {
    OrderItemResponse toOrderItemResponse(OrderDetail o);
}
