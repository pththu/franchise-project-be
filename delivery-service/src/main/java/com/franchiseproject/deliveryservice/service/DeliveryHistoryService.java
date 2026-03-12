package com.franchiseproject.deliveryservice.service;

import com.franchiseproject.deliveryservice.dto.response.DeliveryHistoryResponse;
import com.franchiseproject.deliveryservice.enums.DeliverySatus;
import com.franchiseproject.deliveryservice.model.Delivery;

import java.util.List;
import java.util.UUID;

public interface DeliveryHistoryService {
    void createDeliveryHistory(Delivery delivery);
    List<DeliveryHistoryResponse> getDeliveryHistoryByDeliveryId(UUID deliveryId);

}
