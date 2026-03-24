package com.franchiseproject.apigateway.config;

import com.franchiseproject.apigateway.security.CookieServerBearerTokenConverter;
import com.franchiseproject.apigateway.security.DynamicReactiveAuthorizationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final DynamicReactiveAuthorizationManager dynamicAuthorizationManager;

        @Value("${spring.security.oauth2.client.provider.cognito.issuerUri}")
        private String issuerUri;

        List<String> allowedOrigins = List.of(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "http://0.0.0.0");

        private static final String[] PUBLIC_ENDPOINTS = {
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/verify",
                        "/api/auth/resend-code",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                        "/api/inventory/**",
                        "/api/promotions/**",
                        "/api/auth/change-password",
                        "/api/auth/forgot-password",
                        "/api/auth/forgot-password/confirm",
                        "/api/products/get-all",
                        "/api/products/detail/**",
                        "/api/products/categories/get-all",
                        "/api/products/franchise/**",
                        "/api/franchises",
                        "/api/products/search-by-ids",
                        "/api/products/filter",
        };

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .authorizeExchange(exchange -> exchange
                                                .pathMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                // Mọi request khác đều phải đi qua AuthorizationManager động
                                                .anyExchange().access(dynamicAuthorizationManager))
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .bearerTokenConverter(new CookieServerBearerTokenConverter()) // Đọc
                                                                                                              // token
                                                                                                              // từ
                                                                                                              // Cookie/Header
                                                .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder())) // Verify Token với
                                                                                                  // Cognito
                                );

                return http.build();
        }

        // Tự động tải JWKS từ Cognito để verify chữ ký (Signature) và hạn (Expiration)
        @Bean
        public ReactiveJwtDecoder reactiveJwtDecoder() {
                return NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri).build();
        }

        @Bean
        public CorsWebFilter corsWebFilter() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(allowedOrigins);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return new CorsWebFilter(source);
        }

        // @Bean
        // public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http)
        // {
        //
        // http
        // .csrf(ServerHttpSecurity.CsrfSpec::disable)
        // .authorizeExchange(exchange -> exchange
        // .anyExchange().permitAll()
        // )
        // .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        // .formLogin(ServerHttpSecurity.FormLoginSpec::disable);
        //
        // return http.build();
        // }
}