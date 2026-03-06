package com.franchiseproject.deliveryservice.service;

import com.franchiseproject.deliveryservice.dto.request.CreateDeliveryRequest;
import com.franchiseproject.deliveryservice.dto.response.DeliveryResponse;
import com.franchiseproject.deliveryservice.model.Delivery;
import java.util.List;


public interface DeliveryService {
    List<Delivery> findAll();
    DeliveryResponse createDelivery(CreateDeliveryRequest request);
}
