package com.example.reportservice.dto.response;

import lombok.Data;

@Data
public class DashboardResponse {

    private Double totalRevenue;

    private Long totalOrders;

    private Long activeBranches;

}