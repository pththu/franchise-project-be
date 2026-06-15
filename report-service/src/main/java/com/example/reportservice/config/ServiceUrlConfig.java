package com.example.reportservice.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "services")
@Data
@Slf4j
public class ServiceUrlConfig {

    private Service orderService = new Service();

    @Data
    public static class Service {
        private String url;
        private String path;

        public String getFullUrl() {
            String cleanPath = path != null ? path.replace("/**", "") : "";
            return url + cleanPath;
        }
    }

    @PostConstruct
    public void init() {
        log.info("========== SERVICE URLS CONFIGURATION ==========");
        log.info("Order Service: {}", orderService.getFullUrl());
        log.info("================================================");
    }
}