package com.example.reportservice.client;

import com.example.reportservice.dto.customer.CustomerResponse;
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
public class CustomerServiceClient {

    private final WebClient customerServiceWebClient;

    private static final ParameterizedTypeReference<ApiResponse<List<CustomerResponse>>> CUSTOMER_LIST_TYPE =
            new ParameterizedTypeReference<ApiResponse<List<CustomerResponse>>>() {};

    public Mono<List<CustomerResponse>> getAllCustomers() {
        log.debug("Fetching all customers from customer service");

        return customerServiceWebClient.get()
                .uri("/customers")
                .retrieve()
                .bodyToMono(CUSTOMER_LIST_TYPE)
                .map(ApiResponse::getData)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorResume(e -> {
                    log.error("Error fetching customers: {}", e.getMessage());
                    return Mono.just(Collections.emptyList());
                });
    }
}