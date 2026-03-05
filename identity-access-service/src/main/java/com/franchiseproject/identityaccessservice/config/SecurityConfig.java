package com.franchiseproject.identityaccessservice.config;

import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
@EnableWebSecurity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.provider.cognito.issuerUri}")
    String issuer;
    List<String> allowedOrigins = List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://0.0.0.0"
    );

    final String api_prefix = "/api/auth/";
    final String[] PUBLIC_ENDPOINT = {
            api_prefix + "login",
            api_prefix + "register",
            api_prefix + "introspect",
            api_prefix + "verify",
            api_prefix + "resend-code",
            api_prefix + "refresh"
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
                        .requestMatchers(HttpMethod.GET, ADMIN_ENDPOINT_GET).hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, ADMIN_ENDPOINT_DEL).hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, ADMIN_ENDPOINT_PUT).hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated());


        http.oauth2ResourceServer(oauth2 ->
                oauth2
                        .bearerTokenResolver(bearerTokenResolver())
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
}