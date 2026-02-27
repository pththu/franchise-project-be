package com.franchiseproject.orderservice.mapper;

import com.franchiseproject.orderservice.dto.response.OrderDetailResponse;
import com.franchiseproject.orderservice.model.OrderDetail;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface OrderDetailMapper {
    OrderDetailResponse toOrderDetailResponse(OrderDetail odd);
}
