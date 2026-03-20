package com.example.reportservice.client;

import com.example.reportservice.dto.order.OrderResponse;
import com.example.reportservice.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderServiceClient {

    private final WebClient orderServiceWebClient;

    private static final ParameterizedTypeReference<ApiResponse<List<OrderResponse>>> ORDER_LIST_TYPE =
            new ParameterizedTypeReference<ApiResponse<List<OrderResponse>>>() {};

    public Mono<List<OrderResponse>> getAllOrders() {
        log.debug("Fetching all orders from order service");

        return orderServiceWebClient.get()
                .uri("")
                .retrieve()
                .bodyToMono(ORDER_LIST_TYPE)
                .map(ApiResponse::getData)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorResume(e -> {
                    log.error("Error fetching orders: {}", e.getMessage());
                    return Mono.just(Collections.emptyList());
                });
    }
}