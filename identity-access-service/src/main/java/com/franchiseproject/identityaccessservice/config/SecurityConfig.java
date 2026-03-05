package com.franchiseproject.identityaccessservice.config;

import com.franchiseproject.identityaccessservice.security.DynamicAuthorizationManager;
import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {
    //    @Autowired
    JwtKeyProperties jwtKeyProperties;
    DynamicAuthorizationManager dynamicAuthorizationManager;

//    final String api_prefix = "/api/v1/";
//    final String api_prefix = "/api/auth/";
//    final String[] PUBLIC_ENDPOINT = {

    static String api_prefix = "/api/auth/";
    static String[] PUBLIC_ENDPOINT = {
            api_prefix + "auth/login",
            api_prefix + "auth/register",
            api_prefix + "auth/introspect",
    };

//    final String[] ADMIN_ENDPOINT_GET = {
//            api_prefix + "users"
//    };
//
//    final String[] ADMIN_ENDPOINT_DEL = {
//            api_prefix + "users/delete-account"
//    };
//
//    final String[] ADMIN_ENDPOINT_PUT = {
//            api_prefix + "auth/*/lock"
//    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request ->
                        request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINT).permitAll()

//                        .requestMatchers(HttpMethod.GET, ADMIN_ENDPOINT_GET).hasAuthority("ROLE_ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, ADMIN_ENDPOINT_DEL).hasAuthority("ROLE_ADMIN")
//                        .requestMatchers(HttpMethod.PUT, ADMIN_ENDPOINT_PUT).hasAuthority("ROLE_ADMIN")
//                        .anyRequest().authenticated());

                                .anyRequest().access(dynamicAuthorizationManager)
        );

        http.oauth2ResourceServer(oauth2 ->
                oauth2
                        .bearerTokenResolver(bearerTokenResolver())
                        .jwt(jwtConfigurer -> {
                            try {
                                jwtConfigurer.decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );

        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
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
    JwtDecoder jwtDecoder() throws Exception {
        RSAPublicKey rsaPublicKey = jwtKeyProperties.getPublicKeyObject();
        return NimbusJwtDecoder
                .withPublicKey(rsaPublicKey)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    BearerTokenResolver bearerTokenResolver() {
        return request -> {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) return null;

            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
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