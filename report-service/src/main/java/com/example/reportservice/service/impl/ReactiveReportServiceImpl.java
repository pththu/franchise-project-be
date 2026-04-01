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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveReportServiceImpl implements ReportService {

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
                    List<Map<String, Object>> topCustomers = calculateTopCustomers(orders); // ✅ SỬA
                    List<Map<String, Object>> revenueByBranch = calculateRevenueByBranch(orders);
                    Map<String, Long> orderStatusStats = calculateOrderStatusStats(orders);

                    // Dữ liệu doanh thu theo thời gian
                    Map<String, Object> revenueByTime = calculateRevenueByTimeRange(orders);

                    // Lấy recent orders với đầy đủ thông tin
                    List<Map<String, Object>> recentOrders = orders.stream()
                            .filter(order -> order.getCreateAt() != null)
                            .sorted((a, b) -> {
                                if (a.getCreateAt() == null) return 1;
                                if (b.getCreateAt() == null) return -1;
                                return b.getCreateAt().compareTo(a.getCreateAt());
                            })
                            .limit(10)
                            .map(order -> {
                                Map<String, Object> orderMap = new HashMap<>();
                                orderMap.put("id", order.getId());
                                orderMap.put("createAt", order.getCreateAt());
                                orderMap.put("totalDue", order.getTotalDue());
                                orderMap.put("totalRevenue", order.getTotalDue());
                                orderMap.put("orderStatus", order.getOrderStatus());
                                orderMap.put("customerName", order.getCustomerName() != null ? order.getCustomerName() : "Khách hàng");
                                orderMap.put("franchiseId", order.getFranchiseId());
                                return orderMap;
                            })
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
                    dashboard.put("revenueByTime", revenueByTime);
                    dashboard.put("generatedAt", System.currentTimeMillis());

                    emitReportEvent(Map.of("type", "DASHBOARD_UPDATE", "data", dashboard));

                    return dashboard;
                })
                .onErrorResume(e -> {
                    log.error("Error generating dashboard: {}", e.getMessage());
                    return Mono.just(createErrorDashboard());
                });
    }

    // ✅ SỬA: Tính topCustomers với tên thật từ order
    private List<Map<String, Object>> calculateTopCustomers(List<OrderResponse> orders) {
        Map<UUID, CustomerStats> customerStatsMap = new HashMap<>();
        Map<UUID, String> customerNameMap = new HashMap<>();

        for (OrderResponse order : orders) {
            UUID customerId = order.getCustomerId();
            if (customerId != null && order.getTotalDue() != null) {
                // Gom doanh thu
                customerStatsMap.computeIfAbsent(customerId, k -> new CustomerStats())
                        .addOrder(order.getTotalDue());

                // Lưu tên khách hàng từ order (ưu tiên tên đầu tiên gặp)
                String customerName = order.getCustomerName();
                if (customerName != null && !customerName.isEmpty() && !customerNameMap.containsKey(customerId)) {
                    customerNameMap.put(customerId, customerName);
                }
            }
        }

        // Sắp xếp theo totalSpent giảm dần
        return customerStatsMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().getTotalSpent().compareTo(a.getValue().getTotalSpent()))
                .limit(10)
                .map(entry -> {
                    UUID customerId = entry.getKey();
                    CustomerStats stats = entry.getValue();

                    Map<String, Object> customer = new HashMap<>();
                    customer.put("customerId", customerId);

                    // Lấy tên từ map, nếu không có thì dùng "Khách hàng"
                    String customerName = customerNameMap.get(customerId);
                    if (customerName == null || customerName.isEmpty()) {
                        customerName = "Khách hàng";
                    }
                    customer.put("customerName", customerName);
                    customer.put("fullName", customerName);
                    customer.put("totalOrders", stats.getOrderCount());
                    customer.put("totalSpent", stats.getTotalSpent().doubleValue());

                    return customer;
                })
                .collect(Collectors.toList());
    }

    // ================= Các method còn lại giữ nguyên =================

    private Map<String, Object> calculateRevenueByTimeRange(List<OrderResponse> orders) {
        Map<String, Object> result = new HashMap<>();

        result.put("revenueByTime7", calculateRevenueByDays(orders, 7));
        result.put("revenueByTime14", calculateRevenueByDays(orders, 14));
        result.put("revenueByTime30", calculateRevenueByDays(orders, 30));
        result.put("revenueByTime90", calculateRevenueByDays(orders, 90));
        result.put("revenueByTime365", calculateRevenueByDays(orders, 365));
        result.put("revenueByMonth", calculateRevenueByMonth(orders, 12));

        return result;
    }

    private List<Map<String, Object>> calculateRevenueByDays(List<OrderResponse> orders, int days) {
        Map<String, BigDecimal> revenueByDate = new LinkedHashMap<>();

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            String dateKey = date.format(formatter);
            revenueByDate.put(dateKey, BigDecimal.ZERO);
        }

        for (OrderResponse order : orders) {
            if (order.getCreateAt() != null && order.getTotalDue() != null) {
                LocalDate orderDate = order.getCreateAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                String dateKey = orderDate.format(formatter);

                if (revenueByDate.containsKey(dateKey)) {
                    revenueByDate.put(dateKey,
                            revenueByDate.get(dateKey).add(order.getTotalDue()));
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : revenueByDate.entrySet()) {
            Map<String, Object> dailyRevenue = new HashMap<>();
            dailyRevenue.put("date", entry.getKey());
            dailyRevenue.put("totalRevenue", entry.getValue().doubleValue());
            result.add(dailyRevenue);
        }

        return result;
    }

    private List<Map<String, Object>> calculateRevenueByMonth(List<OrderResponse> orders, int months) {
        Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();

        LocalDate now = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String monthKey = date.format(monthFormatter);
            revenueByMonth.put(monthKey, BigDecimal.ZERO);
        }

        for (OrderResponse order : orders) {
            if (order.getCreateAt() != null && order.getTotalDue() != null) {
                LocalDate orderDate = order.getCreateAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                String monthKey = orderDate.format(monthFormatter);

                if (revenueByMonth.containsKey(monthKey)) {
                    revenueByMonth.put(monthKey,
                            revenueByMonth.get(monthKey).add(order.getTotalDue()));
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : revenueByMonth.entrySet()) {
            Map<String, Object> monthlyRevenue = new HashMap<>();
            monthlyRevenue.put("month", entry.getKey());
            monthlyRevenue.put("totalRevenue", entry.getValue().doubleValue());
            result.add(monthlyRevenue);
        }

        return result;
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
        errorDashboard.put("revenueByTime", Map.of());
        errorDashboard.put("generatedAt", System.currentTimeMillis());

        return errorDashboard;
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

    // Inner classes
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