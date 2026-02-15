package com.franchiseproject.deliveryservice.service.impl;

import com.franchiseproject.deliveryservice.model.Delivery;
import com.franchiseproject.deliveryservice.repository.DeliveryRepository;
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

    @Override
    public List<Delivery> findAll() {
        return deliveryRepository.findAll();
    }

}
