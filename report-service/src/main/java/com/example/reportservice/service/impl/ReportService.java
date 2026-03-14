package com.example.reportservice.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ReportService {

    public Object getOrders() {

        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:3007/api/orders";

        return restTemplate.getForObject(url, Object.class);
    }
}