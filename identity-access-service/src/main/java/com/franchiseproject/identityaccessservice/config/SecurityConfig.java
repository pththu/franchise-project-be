package com.franchiseproject.identityaccessservice.config;

import com.franchiseproject.identityaccessservice.security.DynamicAuthorizationManager;
import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {
    //    @Autowired
//    JwtKeyProperties jwtKeyProperties;
    final DynamicAuthorizationManager dynamicAuthorizationManager;

    @Value("${spring.security.oauth2.client.provider.cognito.issuerUri}")
    String issuer;

    List<String> allowedOrigins = List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://0.0.0.0"
    );

    static String api_prefix = "/api/auth/";

    static String[] PUBLIC_ENDPOINT = {
            api_prefix + "login",      // /api/auth/login
            api_prefix + "register",   // /api/auth/register
            api_prefix + "introspect", // /api/auth/introspect
            api_prefix + "verify",
            api_prefix + "resend-code",
            api_prefix + "refresh",
            api_prefix + "auth/login",
            api_prefix + "auth/register",
            api_prefix + "auth/introspect"
    };

    final String[] ADMIN_ENDPOINT_GET = {
            api_prefix + "users"
    };

    final String[] ADMIN_ENDPOINT_DEL = {
            api_prefix + "users/delete-account"
    };

    final String[] ADMIN_ENDPOINT_PUT = {
            api_prefix + "*/lock"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults());
        http.authorizeHttpRequests(request ->
                        request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINT).permitAll()

//                        .requestMatchers(HttpMethod.GET, ADMIN_ENDPOINT_GET).hasAuthority("ROLE_ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, ADMIN_ENDPOINT_DEL).hasAuthority("ROLE_ADMIN")
//                        .requestMatchers(HttpMethod.PUT, ADMIN_ENDPOINT_PUT).hasAuthority("ROLE_ADMIN")
//                        .anyRequest().authenticated());

                                .anyRequest().access(dynamicAuthorizationManager)
        );

        http.oauth2ResourceServer(oauth2 ->
                        oauth2.bearerTokenResolver(bearerTokenResolver())
                                .jwt(jwtConfigurer -> {
                                    try {
//                                jwtConfigurer.decoder(jwtDecoder())
//                                        .jwtAuthenticationConverter(jwtAuthenticationConverter());
                                        jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter());
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                })
        );

        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Cognito stores groups in "cognito:groups" claim
        grantedAuthoritiesConverter.setAuthoritiesClaimName("cognito:groups");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuer).build();
    }

//    @Bean
//    JwtDecoder jwtDecoder() throws Exception {
//        RSAPublicKey rsaPublicKey = jwtKeyProperties.getPublicKeyObject();
//        return NimbusJwtDecoder
//                .withPublicKey(rsaPublicKey)
//                .build();
//    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

//    @Bean
//    BearerTokenResolver bearerTokenResolver() {
//        return request -> {
//            Cookie[] cookies = request.getCookies();
//            if (cookies == null) return null;
//
//            for (Cookie cookie : cookies) {
//                if ("access_token".equals(cookie.getName())) {
//                    return cookie.getValue();
//                }
//            }
//            return null;
//        };
//    }

    @Bean
    BearerTokenResolver bearerTokenResolver() {
        return request -> {
            // 1. Kiểm tra Header trước (Để bạn test được trên Postman)
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }

            // 2. Kiểm tra Cookie (Để Frontend của đồng nghiệp chạy được)
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("access_token".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            return null;
        };
    }

//    @Bean
//    BearerTokenResolver bearerTokenResolver() {
//        return request -> {
//            String bearerToken = request.getHeader("Authorization");
//            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//                return bearerToken.substring(7);
//            }
//
//            Cookie[] cookies = request.getCookies();
//            if (cookies != null) {
//                for (Cookie cookie : cookies) {
//                    if ("access_token".equals(cookie.getName())) {
//                        return cookie.getValue();
//                    }
//                }
//            }
//            return null;
//        };
//    }

//    @Bean
//    BearerTokenResolver bearerTokenResolver() {
//        return request -> {
//            String bearerToken = request.getHeader("Authorization");
//            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//                return bearerToken.substring(7);
//            }
//
//            Cookie[] cookies = request.getCookies();
//            if (cookies != null) {
//                for (Cookie cookie : cookies) {
//                    if ("access_token".equals(cookie.getName())) {
//                        return cookie.getValue();
//                    }
//                }
//            }
//            return null;
//        };
//    }
}