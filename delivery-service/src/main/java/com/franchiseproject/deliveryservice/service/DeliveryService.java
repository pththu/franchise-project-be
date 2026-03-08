package com.franchiseproject.deliveryservice.service;

import com.franchiseproject.deliveryservice.dto.request.CreateDeliveryRequest;
import com.franchiseproject.deliveryservice.dto.request.UpdateDeliveryRequest;
import com.franchiseproject.deliveryservice.dto.response.DeliveryResponse;
import com.franchiseproject.deliveryservice.model.Delivery;
import java.util.List;
import java.util.UUID;


public interface DeliveryService {
    List<Delivery> findAll();
    DeliveryResponse createDelivery(CreateDeliveryRequest request);
    DeliveryResponse assignShipper(UUID deliveryId, UpdateDeliveryRequest request);
}
