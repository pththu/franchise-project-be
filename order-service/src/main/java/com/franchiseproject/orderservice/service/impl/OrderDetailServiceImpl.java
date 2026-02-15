package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.model.OrderDetail;
import com.franchiseproject.orderservice.repository.OrderDetailRepository;
import com.franchiseproject.orderservice.service.OrderDetailService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailServiceImpl implements OrderDetailService {
    OrderDetailRepository orderDetailRepository;

    public List<OrderDetail> getAll() {
        return orderDetailRepository.findAll();
    }
}
