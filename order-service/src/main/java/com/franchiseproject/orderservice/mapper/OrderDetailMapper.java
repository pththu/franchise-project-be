package com.franchiseproject.orderservice.mapper;

import com.franchiseproject.orderservice.dto.OrderItemResponse;
import com.franchiseproject.orderservice.model.OrderDetail;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface OrderDetailMapper {
    OrderItemResponse toOrderItemResponse(OrderDetail o);
}
