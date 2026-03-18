package com.franchiseproject.deliveryservice.mapper;

import com.franchiseproject.deliveryservice.dto.response.DeliveryHistoryResponse;
import com.franchiseproject.deliveryservice.model.DeliveryHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeliveryHistoryMapper {
    DeliveryHistoryResponse toDeliveryHistoryResponse(DeliveryHistory deliveryHistory);
}
