package com.franchiseproject.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // CHỈ CẦN CÁI NÀY
        config.setAllowedMethods(Arrays.asList("*"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}


//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//
//        // Cho phép frontend - THÊM port 5173
//        config.setAllowedOrigins(Arrays.asList(
//                "http://localhost:3000",     // React dev (nếu dùng CRA)
//                "http://localhost:3001",     // React alternate
//                "http://localhost:5173",     // ← VITE DEFAULT PORT (THIẾU CÁI NÀY)
//                "http://localhost:5174",     // Vite alternate
//                "http://127.0.0.1:3000",
//                "http://127.0.0.1:5173"      // ← Localhost IP với port 5173
//        ));
//
//        // Cho phép tất cả methods cần thiết
//        config.setAllowedMethods(Arrays.asList(
//                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
//        ));
//
//        // Headers cho phép
//        config.setAllowedHeaders(Arrays.asList(
//                "Origin",
//                "Content-Type",
//                "Accept",
//                "Authorization",
//                "X-Requested-With",
//                "X-User-ID",
//                "X-Franchise-ID"
//        ));
//
//        // Headers expose cho frontend
//        config.setExposedHeaders(Arrays.asList(
//                "Authorization",
//                "X-Total-Count"
//        ));
//
//        config.setAllowCredentials(true);
//        config.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//
//        return new CorsWebFilter(source);
//    }
//}
