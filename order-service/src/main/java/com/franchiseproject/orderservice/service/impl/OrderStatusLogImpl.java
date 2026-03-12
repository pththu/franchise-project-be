package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.entity.OrderStatusLog;
import com.franchiseproject.orderservice.repository.OrderStatusLogRepository;
import com.franchiseproject.orderservice.service.OrderStatusLogService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatusLogImpl implements OrderStatusLogService {
    OrderStatusLogRepository orderStatusLogRepository;

    public List<OrderStatusLog> getAll() {
        return orderStatusLogRepository.findAll();
    }
}
