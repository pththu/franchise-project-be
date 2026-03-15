package com.example.reportservice.service.impl;

import com.example.reportservice.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Object> getDashboard() {

        Map<String, Object> dashboard = new HashMap<>();

        String orderUrl = "http://localhost:3007/api/orders";

        Map orderResponse = restTemplate.getForObject(orderUrl, Map.class);

        List orders = (List) orderResponse.get("data");

        int totalOrders = orders.size();

        double totalRevenue = 0;

        for (Object obj : orders) {

            Map order = (Map) obj;

            Object totalDue = order.get("totalDue");

            if (totalDue != null) {
                totalRevenue += Double.parseDouble(totalDue.toString());
            }
        }

        dashboard.put("totalOrders", totalOrders);
        dashboard.put("totalRevenue", totalRevenue);
        dashboard.put("orders", orders);

        return dashboard;
    }
}