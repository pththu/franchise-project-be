package com.example.reportservice.service.impl;

import com.example.reportservice.client.OrderServiceClient;
import com.example.reportservice.dto.order.OrderResponse;
import com.example.reportservice.dto.order.OrderItemResponse;
import com.example.reportservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class    ReactiveReportServiceImpl implements ReportService {

    private final OrderServiceClient orderClient;

    private final Sinks.Many<Object> reportSink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public Mono<Map<String, Object>> getDashboard() {
        log.info("Generating comprehensive dashboard");

        long startTime = System.currentTimeMillis();

        return orderClient.getAllOrders()
                .subscribeOn(Schedulers.parallel())
                .map(orders -> {
                    log.info("Received {} orders", orders.size());

                    Map<String, Object> summary = calculateSummary(orders);
                    List<Map<String, Object>> topProducts = calculateTopProducts(orders);
                    List<Map<String, Object>> topCustomers = calculateTopCustomers(orders);
                    List<Map<String, Object>> revenueByBranch = calculateRevenueByBranch(orders);
                    Map<String, Long> orderStatusStats = calculateOrderStatusStats(orders);

                    List<OrderResponse> recentOrders = orders.stream()
                            .sorted((a, b) -> {
                                if (a.getCreateAt() == null) return 1;
                                if (b.getCreateAt() == null) return -1;
                                return b.getCreateAt().compareTo(a.getCreateAt());
                            })
                            .limit(10)
                            .collect(Collectors.toList());

                    long duration = System.currentTimeMillis() - startTime;
                    log.info("Dashboard generated in {} ms", duration);

                    Map<String, Object> dashboard = new HashMap<>();
                    dashboard.put("summary", summary);
                    dashboard.put("topProducts", topProducts);
                    dashboard.put("topCustomers", topCustomers);
                    dashboard.put("revenueByBranch", revenueByBranch);
                    dashboard.put("orderStatusStats", orderStatusStats);
                    dashboard.put("recentOrders", recentOrders);
                    dashboard.put("generatedAt", System.currentTimeMillis());

                    emitReportEvent(Map.of("type", "DASHBOARD_UPDATE", "data", dashboard));

                    return dashboard;
                })
                .onErrorResume(e -> {
                    log.error("Error generating dashboard: {}", e.getMessage());
                    return Mono.just(createErrorDashboard());
                });
    }

    @Override
    public Flux<ServerSentEvent<Object>> getReportEvents() {
        return reportSink.asFlux()
                .map(event -> ServerSentEvent.<Object>builder()
                        .id(UUID.randomUUID().toString())
                        .event("report-update")
                        .data(event)
                        .build());
    }

    public void emitReportEvent(Object eventData) {
        reportSink.tryEmitNext(eventData);
    }

    private Map<String, Object> calculateSummary(List<OrderResponse> orders) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalOrders", orders.size());

        BigDecimal totalRevenue = orders.stream()
                .map(OrderResponse::getTotalDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalRevenue", totalRevenue.doubleValue());

        long activeBranches = orders.stream()
                .map(OrderResponse::getFranchiseId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        summary.put("activeBranches", activeBranches);

        long totalProducts = orders.stream()
                .filter(o -> o.getOrderDetails() != null)
                .flatMap(o -> o.getOrderDetails().stream())
                .map(OrderItemResponse::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        summary.put("totalProducts", totalProducts);

        long totalCustomers = orders.stream()
                .map(OrderResponse::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        summary.put("totalCustomers", totalCustomers);

        return summary;
    }

    private List<Map<String, Object>> calculateTopProducts(List<OrderResponse> orders) {
        Map<UUID, ProductSales> productSalesMap = new HashMap<>();

        for (OrderResponse order : orders) {
            if (order.getOrderDetails() != null) {
                for (OrderItemResponse item : order.getOrderDetails()) {
                    productSalesMap.computeIfAbsent(item.getProductId(), k -> new ProductSales())
                            .addSale(
                                    item.getProductNameSnapshot() != null ? item.getProductNameSnapshot() : "Unknown Product",
                                    item.getQuantity(),
                                    item.getCost()
                            );
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

    private List<Map<String, Object>> calculateTopCustomers(List<OrderResponse> orders) {
        Map<UUID, CustomerStats> customerStatsMap = new HashMap<>();

        for (OrderResponse order : orders) {
            if (order.getCustomerId() != null) {
                customerStatsMap.computeIfAbsent(order.getCustomerId(), k -> new CustomerStats())
                        .addOrder(order.getTotalDue());
            }
        }

        return customerStatsMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().getTotalSpent().compareTo(a.getValue().getTotalSpent()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("customerId", entry.getKey());
                    customer.put("customerName", "Customer " + entry.getKey().toString().substring(0, 8));
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
                    branch.put("branchName", "Branch " + entry.getKey().toString().substring(0, 8));
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
                        status -> status,
                        Collectors.counting()
                ));
    }

    private Map<String, Object> createErrorDashboard() {
        Map<String, Object> errorDashboard = new HashMap<>();
        errorDashboard.put("error", true);
        errorDashboard.put("message", "Unable to fetch data from order service");

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

    private static class ProductSales {
        private String productName;
        private Integer totalSold = 0;
        private BigDecimal totalRevenue = BigDecimal.ZERO;

        void addSale(String name, Integer quantity, BigDecimal revenue) {
            this.productName = name;
            if (quantity != null) {
                this.totalSold += quantity;
            }
            if (revenue != null) {
                this.totalRevenue = this.totalRevenue.add(revenue);
            }
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