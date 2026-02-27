package com.franchiseproject.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    public RestClient productRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:3000") // port product-service https://4d40e83b-ecfb-448f-9862-7bf7ac5115f0.mock.pstmn.io
                .build();
    }
}
