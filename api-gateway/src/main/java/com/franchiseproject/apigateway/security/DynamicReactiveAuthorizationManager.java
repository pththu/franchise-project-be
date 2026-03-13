package com.franchiseproject.apigateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicReactiveAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    // WebClient để gọi HTTP non-blocking xuống Identity Service
    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {

        ServerHttpRequest request = object.getExchange().getRequest();
        String requestPath = request.getPath().value();
        String requestMethod = request.getMethod().name();
        log.info("orequest {}", object.getExchange().getRequest());
        log.info("requestPath {}", request.getPath().value());
        log.info("requestMethod {}", request.getMethod().name());

        return authentication
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    // Trích xuất Role từ Jwt
                    if (auth.getPrincipal() instanceof Jwt jwt) {
                        List<String> groups = jwt.getClaimAsStringList("cognito:groups");
                        String roleName = (groups != null && !groups.isEmpty()) ? groups.get(0) : "";

                        log.info("roleName - {}", roleName);
                        if (roleName.isEmpty()) {
                            return Mono.just(new AuthorizationDecision(false));
                        }

                        // Gọi xuống Identity Service để check quyền
                        // LƯU Ý: Ở Production, bạn NÊN áp dụng Redis Cache tại đây để tránh spam request
                        return checkPermissionFromIdentityService(roleName, requestPath, requestMethod)
                                .map(AuthorizationDecision::new);
                    }
                    return Mono.just(new AuthorizationDecision(false));
                })
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    private Mono<Boolean> checkPermissionFromIdentityService(String role, String path, String method) {
        // URL của Identity Service (nếu dùng Eureka/Consul thì thay bằng tên service)
        String identityServiceUrl = "http://localhost:3004/api/auth/internal/permissions/check";

        return webClientBuilder.build().get()
                // 2. Tái sử dụng biến và truyền param cực kỳ ngắn gọn, an toàn
                .uri(identityServiceUrl + "?roleName={role}&path={path}&method={method}", role, path, method)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(e -> {
                    log.error("Lỗi khi gọi Identity Service để check quyền: {}", e.getMessage());
                    return Mono.just(false); // Trả về false (chặn quyền) nếu service kia sập
                });
    }

    @Override
    public Mono<Void> verify(Mono<Authentication> authentication, AuthorizationContext object) {
        return ReactiveAuthorizationManager.super.verify(authentication, object);
    }
}