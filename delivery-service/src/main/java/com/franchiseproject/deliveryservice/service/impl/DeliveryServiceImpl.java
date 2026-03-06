package com.franchiseproject.deliveryservice.service.impl;

import com.franchiseproject.deliveryservice.dto.request.CreateDeliveryRequest;
import com.franchiseproject.deliveryservice.dto.response.DeliveryHistoryResponse;
import com.franchiseproject.deliveryservice.dto.response.DeliveryResponse;
import com.franchiseproject.deliveryservice.enums.DeliverySatus;
import com.franchiseproject.deliveryservice.mapper.DeliveryMapper;
import com.franchiseproject.deliveryservice.model.Delivery;
import com.franchiseproject.deliveryservice.repository.DeliveryRepository;
import com.franchiseproject.deliveryservice.service.DeliveryHistoryService;
import com.franchiseproject.deliveryservice.service.DeliveryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class DeliveryServiceImpl implements DeliveryService {
    DeliveryRepository deliveryRepository;
    DeliveryMapper deliveryMapper;
    DeliveryHistoryService deliveryHistoryService;

    @Override
    public List<Delivery> findAll() {
        return deliveryRepository.findAll();
    }

    @Override
    public DeliveryResponse createDelivery(CreateDeliveryRequest request) {
        Delivery delivery = Delivery.builder()
                .orderId(request.getOrderId())
                .staffId(request.getStaffId())
                .weight(request.getWeight())
                .scheduledAt(request.getScheduledAt())
                .status(DeliverySatus.CREATED)
                .build();
        delivery = deliveryRepository.save(delivery);

        deliveryHistoryService.createDeliveryHistory(delivery);

        List<DeliveryHistoryResponse> histories =
                deliveryHistoryService.getDeliveryHistoryByDeliveryId(delivery.getDeliveryId());

        DeliveryResponse response = deliveryMapper.toDeliveryResponse(delivery);
        response.setDeliveryHistory(histories);
        return response;
    }

}
