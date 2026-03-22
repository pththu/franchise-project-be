package com.example.reportservice.client;

import com.example.reportservice.dto.product.ProductResponse;
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
public class ProductServiceClient {

    private final WebClient productServiceWebClient;

    private static final ParameterizedTypeReference<ApiResponse<List<ProductResponse>>> PRODUCT_LIST_TYPE =
            new ParameterizedTypeReference<ApiResponse<List<ProductResponse>>>() {};

    public Mono<List<ProductResponse>> getAllProducts() {
        log.debug("Fetching all products from product service");

        return productServiceWebClient.get()
                .uri("")
                .retrieve()
                .bodyToMono(PRODUCT_LIST_TYPE)
                .map(ApiResponse::getData)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorResume(e -> {
                    log.error("Error fetching products: {}", e.getMessage());
                    return Mono.just(Collections.emptyList());
                });
    }
}