package com.example.reportservice.dto.response;

import lombok.Data;

@Data
public class DashboardResponse {
    private Double totalRevenue;    //Tổng doanh thu
    private Long totalOrders;       //Tổng đơn hàng
    private Long activeBranches;    //Số chi nhánh active
    private Long totalProducts;     //Tổng sản phẩm
    private Long totalCustomers;    //Tổng khách hàng
}