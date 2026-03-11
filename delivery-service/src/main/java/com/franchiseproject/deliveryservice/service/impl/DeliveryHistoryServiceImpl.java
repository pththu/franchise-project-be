package com.franchiseproject.deliveryservice.service.impl;

import com.franchiseproject.deliveryservice.dto.response.DeliveryHistoryResponse;
import com.franchiseproject.deliveryservice.enums.DeliverySatus;
import com.franchiseproject.deliveryservice.mapper.DeliveryHistoryMapper;
import com.franchiseproject.deliveryservice.model.Delivery;
import com.franchiseproject.deliveryservice.model.DeliveryHistory;
import com.franchiseproject.deliveryservice.repository.DeliveryHistoryRepository;
import com.franchiseproject.deliveryservice.service.DeliveryHistoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class DeliveryHistoryServiceImpl implements DeliveryHistoryService {
    DeliveryHistoryRepository deliveryHistoryRepository;
    DeliveryHistoryMapper deliveryHistoryMapper;

    @Override
    public void createDeliveryHistory(Delivery delivery, UUID staffId) {
        Instant now;
        if (delivery.getStatus().equals(DeliverySatus.DELIVERED)) {
            now = Instant.now();
        } else {
            now = null;
        }
        String note = noteHandler(delivery, now);
        DeliveryHistory history = DeliveryHistory.builder()
                .delivery(delivery)
                .status(delivery.getStatus())
                .note(note)
                .updatedBy(staffId)
                .receivedAt(delivery.getStatus().equals(DeliverySatus.DELIVERED) ? Instant.now() : null)
                .build();
        deliveryHistoryRepository.save(history);
    }

    @Override
    public List<DeliveryHistoryResponse> getDeliveryHistoryByDeliveryId(UUID deliveryId) {
        List<DeliveryHistory> list = deliveryHistoryRepository.findByDelivery_DeliveryId(deliveryId);
        return list.stream().map(deliveryHistoryMapper::toDeliveryHistoryResponse).toList();
    }

    private String noteHandler(Delivery delivery, Instant time) {
        return switch (delivery.getStatus()) {
            case CREATED -> "Delivery created";
            case ASSIGNED -> "Delivery assigned to shipper with id: " + delivery.getShipperId();
            case SHIPPING -> "Delivery is on the way";
            case DELIVERED -> {
                if (time != null) {
                    if (time.isBefore(delivery.getScheduledAt())) {
                        yield "Delivery delivered on time";
                    } else {
                        yield "Delivery delivered late";
                    }
                }
                yield "Delivery delivered on time";
            }
            case FAILED -> "Delivery failed";
            case RATING -> "Delivery rated";
        };
    }
}
