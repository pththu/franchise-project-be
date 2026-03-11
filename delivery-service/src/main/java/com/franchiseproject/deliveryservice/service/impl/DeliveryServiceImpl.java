package com.franchiseproject.deliveryservice.service.impl;

import com.franchiseproject.deliveryservice.dto.request.CreateDeliveryRequest;
import com.franchiseproject.deliveryservice.dto.request.UpdateDeliveryRequest;
import com.franchiseproject.deliveryservice.dto.response.DeliveryHistoryResponse;
import com.franchiseproject.deliveryservice.dto.response.DeliveryResponse;
import com.franchiseproject.deliveryservice.enums.DeliverySatus;
import com.franchiseproject.deliveryservice.exception.AppException;
import com.franchiseproject.deliveryservice.exception.ErrorCode;
import com.franchiseproject.deliveryservice.mapper.DeliveryMapper;
import com.franchiseproject.deliveryservice.model.Delivery;
import com.franchiseproject.deliveryservice.repository.DeliveryRepository;
import com.franchiseproject.deliveryservice.service.DeliveryHistoryService;
import com.franchiseproject.deliveryservice.service.DeliveryService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;


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
                .shipperId(request.getShipperId())
                .weight(request.getWeight())
                .scheduledAt(request.getScheduledAt())
                .status(DeliverySatus.CREATED)
                .build();

        delivery = deliveryRepository.save(delivery);

        return buildResponse(delivery, request.getStaffId());
    }

    @Override
    @Transactional
    public DeliveryResponse updateDelivery(UUID deliveryId, UpdateDeliveryRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_NOT_FOUND));
        if (delivery.getStatus() == DeliverySatus.DELIVERED
                || delivery.getStatus() == DeliverySatus.FAILED
                || delivery.getStatus() == DeliverySatus.RATING) {
            throw new AppException(ErrorCode.DELIVERY_ALREADY_FINALIZED);
        }
        delivery.setShipperId(request.getShipperId() == null ? delivery.getShipperId() : request.getShipperId());
        delivery.setWeight(request.getWeight() == 0 ? delivery.getWeight() : request.getWeight());
        delivery.setScheduledAt(request.getScheduledAt() == null ? delivery.getScheduledAt() : request.getScheduledAt());
        delivery.setStatus(request.getStatus());

        delivery = deliveryRepository.save(delivery);

        return buildResponse(delivery, request.getStaffId());
    }

    @Override
    public DeliveryResponse getDeliveryByOrderId(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId);
        if (delivery == null) {
            throw new AppException(ErrorCode.DELIVERY_NOT_FOUND);
        }
        return deliveryMapper.toDeliveryResponse(delivery);
    }

    private DeliveryResponse buildResponse(Delivery delivery, UUID staffId) {
        deliveryHistoryService.createDeliveryHistory(delivery, staffId);

        List<DeliveryHistoryResponse> histories =
                deliveryHistoryService.getDeliveryHistoryByDeliveryId(delivery.getDeliveryId());

        DeliveryResponse response = deliveryMapper.toDeliveryResponse(delivery);
        response.setDeliveryHistory(histories);
        return response;
    }
}
