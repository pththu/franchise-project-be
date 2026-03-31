package com.example.reportservice.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.codec.ServerSentEvent;

import java.util.Map;

public interface ReportService {

    Mono<Map<String, Object>> getDashboard();

    // ================= SSE REAL-TIME =================
    Flux<ServerSentEvent<Object>> getReportEvents();
}