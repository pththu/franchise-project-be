package com.example.reportservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public Double getTotalRevenue() {

        String sql = """
            SELECT COALESCE(SUM(total_amount),0)
            FROM orders
        """;

        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public Long getTotalOrders() {

        String sql = """
            SELECT COUNT(*)
            FROM orders
        """;

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getActiveBranches() {

        String sql = """
            SELECT COUNT(*)
            FROM franchise
            WHERE status = 'ACTIVE'
        """;

        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}