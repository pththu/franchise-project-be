package com.franchiseproject.deliveryservice.controller;

import com.franchiseproject.deliveryservice.model.Delivery;
import com.franchiseproject.deliveryservice.service.DeliveryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@RequestMapping("/delivery")
public class DeliveryController {
    DeliveryService deliveryService;

    @GetMapping("/getall")
    public List<Delivery> findAll() {
        return deliveryService.findAll();
    }
}

