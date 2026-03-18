package com.franchiseproject.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    public RestClient apiRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:3000")
                .build();
    }
}
