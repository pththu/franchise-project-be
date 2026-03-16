package com.franchiseproject.paymentservice.config;

import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final MomoProperties momoProperties;

    /// Call MOMO
    @Bean
    public RestClient momoRestClient() {
        return RestClient.builder()
                .baseUrl(momoProperties.getEnd_point())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {

                    log.info("Calling MoMo API: {}", request.getURI());
                    log.info("Method: {}", request.getMethod());
                    log.info("Body: {}", body);

                    return execution.execute(request, body);
                })
                .build();
    }


    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:3007")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
