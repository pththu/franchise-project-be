package com.franchiseproject.deliveryservice.mapper;

import com.franchiseproject.deliveryservice.dto.response.DeliveryResponse;
import com.franchiseproject.deliveryservice.model.Delivery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {DeliveryHistoryMapper.class})
public interface DeliveryMapper {
    DeliveryResponse toDeliveryResponse(Delivery delivery);
}
