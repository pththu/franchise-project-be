package com.example.reportservice.service;

import reactor.core.publisher.Mono;
import java.util.Map;

public interface ReportService {
    Mono<Map<String, Object>> getDashboard();  // Trả về Mono
}