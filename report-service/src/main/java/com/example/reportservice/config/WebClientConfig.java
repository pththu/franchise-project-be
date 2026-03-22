package com.example.reportservice.config;

import com.example.reportservice.client.*;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebClientConfig {
    
    private final ServiceUrlConfig urlConfig;
    
    @Value("${webclient.connection-timeout:5000}")
    private int connectionTimeout;
    
    @Value("${webclient.read-timeout:10000}")
    private int readTimeout;
    
    @Bean
    public HttpClient httpClient() {
        ConnectionProvider provider = ConnectionProvider.builder("report-service-pool")
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(30))
            .build();
        
        return HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
            .responseTimeout(Duration.ofMillis(readTimeout))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
            );
    }
    
    @Bean
    public WebClient.Builder webClientBuilder(HttpClient httpClient) {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(16 * 1024 * 1024))
            .filter(logRequest())
            .filter(logResponse());
    }
    
    /**
     * Tạo WebClient riêng cho từng service
     */
    @Bean
    public WebClient orderServiceWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl(urlConfig.getOrderService().getFullUrl())
            .build();
    }
    
    @Bean
    public WebClient productServiceWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl(urlConfig.getProductService().getFullUrl())
            .build();
    }
    
    @Bean
    public WebClient customerServiceWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl(urlConfig.getCustomerService().getFullUrl())
            .build();
    }
    
    // Branch service - để sau khi có service
    // @Bean
    // public WebClient branchServiceWebClient(WebClient.Builder builder) {
    //     return builder
    //         .baseUrl(urlConfig.getBranchService().getFullUrl())
    //         .build();
    // }
    
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.debug("Request: {} {}", request.method(), request.url());
            return Mono.just(request);
        });
    }
    
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.debug("Response status: {}", response.statusCode());
            return Mono.just(response);
        });
    }
}