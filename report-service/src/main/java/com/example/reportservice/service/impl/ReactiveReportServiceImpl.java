package com.example.reportservice.service.impl;

import com.example.reportservice.client.*;
import com.example.reportservice.dto.order.OrderResponse;
import com.example.reportservice.dto.order.OrderItemResponse;
import com.example.reportservice.dto.product.ProductResponse;
import com.example.reportservice.dto.customer.CustomerResponse;
import com.example.reportservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveReportServiceImpl implements ReportService {

    private final OrderServiceClient orderClient;
    private final ProductServiceClient productClient;
    private final CustomerServiceClient customerClient;
    // private final BranchServiceClient branchClient; // Thêm sau

    @Override
    public Mono<Map<String, Object>> getDashboard() {
        log.info("Generating comprehensive dashboard");

        long startTime = System.currentTimeMillis();

        // Gọi song song tất cả services
        Mono<List<OrderResponse>> ordersMono = orderClient.getAllOrders()
                .subscribeOn(Schedulers.parallel());

        Mono<List<ProductResponse>> productsMono = productClient.getAllProducts()
                .subscribeOn(Schedulers.parallel());

        Mono<List<CustomerResponse>> customersMono = customerClient.getAllCustomers()
                .subscribeOn(Schedulers.parallel());

        return Mono.zip(ordersMono, productsMono, customersMono)
                .map(tuple -> {
                    List<OrderResponse> orders = tuple.getT1();
                    List<ProductResponse> products = tuple.getT2();
                    List<CustomerResponse> customers = tuple.getT3();

                    log.info("Received {} orders, {} products, {} customers",
                            orders.size(), products.size(), customers.size());

                    // 1. Tính summary
                    Map<String, Object> summary = calculateSummary(orders, products, customers);

                    // 2. Tính top products
                    List<Map<String, Object>> topProducts = calculateTopProducts(orders);

                    // 3. Tính top customers
                    List<Map<String, Object>> topCustomers = calculateTopCustomers(orders, customers);

                    // 4. Tính revenue by branch
                    List<Map<String, Object>> revenueByBranch = calculateRevenueByBranch(orders);

                    // 5. Tính order status stats
                    Map<String, Long> orderStatusStats = calculateOrderStatusStats(orders);

                    // 6. Lấy recent orders (top 10)
                    List<OrderResponse> recentOrders = orders.stream()
                            .sorted((a, b) -> {
                                if (a.getCreateAt() == null) return 1;
                                if (b.getCreateAt() == null) return -1;
                                return b.getCreateAt().compareTo(a.getCreateAt());
                            })
                            .limit(10)
                            .collect(Collectors.toList());

                    long duration = System.currentTimeMillis() - startTime;
                    log.info(" Dashboard generated in {} ms", duration);

                    // Build final response
                    Map<String, Object> dashboard = new HashMap<>();
                    dashboard.put("summary", summary);
                    dashboard.put("topProducts", topProducts);
                    dashboard.put("topCustomers", topCustomers);
                    dashboard.put("revenueByBranch", revenueByBranch);
                    dashboard.put("orderStatusStats", orderStatusStats);
                    dashboard.put("recentOrders", recentOrders);
                    dashboard.put("generatedAt", System.currentTimeMillis());

                    return dashboard;
                })
                .onErrorResume(e -> {
                    log.error("Error generating dashboard: {}", e.getMessage());
                    return Mono.just(createErrorDashboard());
                });
    }

    private Map<String, Object> calculateSummary(List<OrderResponse> orders,
                                                 List<ProductResponse> products,
                                                 List<CustomerResponse> customers) {
        Map<String, Object> summary = new HashMap<>();

        // Tổng số đơn hàng
        summary.put("totalOrders", orders.size());

        // Tổng doanh thu
        BigDecimal totalRevenue = orders.stream()
                .map(OrderResponse::getTotalDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalRevenue", totalRevenue.doubleValue());

        // Số chi nhánh active
        long activeBranches = orders.stream()
                .map(OrderResponse::getFranchiseId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        summary.put("activeBranches", activeBranches);

        // Tổng số sản phẩm
        summary.put("totalProducts", products.size());

        // Tổng số khách hàng
        summary.put("totalCustomers", customers.size());

        return summary;
    }

    private List<Map<String, Object>> calculateTopProducts(List<OrderResponse> orders) {
        Map<UUID, ProductSales> productSalesMap = new HashMap<>();

        for (OrderResponse order : orders) {
            if (order.getOrderDetails() != null) {
                for (OrderItemResponse item : order.getOrderDetails()) {
                    productSalesMap.computeIfAbsent(item.getProductId(), k -> new ProductSales())
                            .addSale(item.getProductNameSnapshot(), item.getQuantity(), item.getCost());
                }
            }
        }

        return productSalesMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().getTotalSold().compareTo(a.getValue().getTotalSold()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", entry.getKey());
                    product.put("productName", entry.getValue().getProductName());
                    product.put("totalSold", entry.getValue().getTotalSold());
                    product.put("totalRevenue", entry.getValue().getTotalRevenue().doubleValue());
                    return product;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> calculateTopCustomers(List<OrderResponse> orders,
                                                            List<CustomerResponse> customers) {
        Map<UUID, CustomerStats> customerStatsMap = new HashMap<>();

        for (OrderResponse order : orders) {
            if (order.getCustomerId() != null) {
                customerStatsMap.computeIfAbsent(order.getCustomerId(), k -> new CustomerStats())
                        .addOrder(order.getTotalDue());
            }
        }

        Map<UUID, String> customerNameMap = customers.stream()
                .collect(Collectors.toMap(
                        c -> c.getId(),
                        c -> c.getFullName() != null ? c.getFullName() : "Unknown",
                        (a, b) -> a
                ));

        return customerStatsMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().getTotalSpent().compareTo(a.getValue().getTotalSpent()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("customerId", entry.getKey());
                    customer.put("customerName", customerNameMap.getOrDefault(entry.getKey(), "Unknown"));
                    customer.put("totalOrders", entry.getValue().getOrderCount());
                    customer.put("totalSpent", entry.getValue().getTotalSpent().doubleValue());
                    return customer;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> calculateRevenueByBranch(List<OrderResponse> orders) {
        Map<UUID, BranchRevenue> branchRevenueMap = new HashMap<>();

        for (OrderResponse order : orders) {
            if (order.getFranchiseId() != null) {
                branchRevenueMap.computeIfAbsent(order.getFranchiseId(), k -> new BranchRevenue())
                        .addRevenue(order.getTotalDue());
            }
        }

        return branchRevenueMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> branch = new HashMap<>();
                    branch.put("branchId", entry.getKey());
                    branch.put("branchName", "Branch " + entry.getKey().toString().substring(0, 4)); // Tạm thời
                    branch.put("totalOrders", entry.getValue().getOrderCount());
                    branch.put("totalRevenue", entry.getValue().getTotalRevenue().doubleValue());
                    return branch;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Long> calculateOrderStatusStats(List<OrderResponse> orders) {
        return orders.stream()
                .map(OrderResponse::getOrderStatus)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        status -> status.toString(),
                        Collectors.counting()
                ));
    }

    private Map<String, Object> createErrorDashboard() {
        Map<String, Object> errorDashboard = new HashMap<>();
        errorDashboard.put("error", true);
        errorDashboard.put("message", "Unable to fetch data from services");

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", 0);
        summary.put("totalRevenue", 0.0);
        summary.put("activeBranches", 0L);
        summary.put("totalProducts", 0);
        summary.put("totalCustomers", 0);

        errorDashboard.put("summary", summary);
        errorDashboard.put("topProducts", List.of());
        errorDashboard.put("topCustomers", List.of());
        errorDashboard.put("revenueByBranch", List.of());
        errorDashboard.put("orderStatusStats", Map.of());
        errorDashboard.put("recentOrders", List.of());
        errorDashboard.put("generatedAt", System.currentTimeMillis());

        return errorDashboard;
    }

    // Inner classes for calculations
    private static class ProductSales {
        private String productName;
        private Integer totalSold = 0;
        private BigDecimal totalRevenue = BigDecimal.ZERO;

        void addSale(String name, Integer quantity, BigDecimal revenue) {
            this.productName = name;
            this.totalSold += quantity;
            this.totalRevenue = this.totalRevenue.add(revenue);
        }

        public String getProductName() { return productName; }
        public Integer getTotalSold() { return totalSold; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
    }

    private static class CustomerStats {
        private Integer orderCount = 0;
        private BigDecimal totalSpent = BigDecimal.ZERO;

        void addOrder(BigDecimal amount) {
            this.orderCount++;
            if (amount != null) {
                this.totalSpent = this.totalSpent.add(amount);
            }
        }

        public Integer getOrderCount() { return orderCount; }
        public BigDecimal getTotalSpent() { return totalSpent; }
    }

    private static class BranchRevenue {
        private Integer orderCount = 0;
        private BigDecimal totalRevenue = BigDecimal.ZERO;

        void addRevenue(BigDecimal amount) {
            this.orderCount++;
            if (amount != null) {
                this.totalRevenue = this.totalRevenue.add(amount);
            }
        }

        public Integer getOrderCount() { return orderCount; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
    }
}