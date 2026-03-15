package com.franchiseproject.apigateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class CookieServerBearerTokenConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        // 1. Ưu tiên đọc từ Header (Dùng cho Postman / App Mobile)
        log.info("1");

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("2");

        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            log.info("3");

            String token = authorization.substring(7);
            return Mono.just(new BearerTokenAuthenticationToken(token));
        }

        log.info("4");
        // 2. Nếu Header không có, đọc từ Cookie (Dùng cho Web Frontend)
        log.info("exchange {}", exchange.getRequest().getCookies());
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("access_token");
        log.info("cookie: {}", cookie);
        log.info("5");
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            log.info("6 {}", StringUtils.hasText(cookie.getValue()));
            return Mono.just(new BearerTokenAuthenticationToken(cookie.getValue()));
        }

        // Không tìm thấy token, trả về empty để luồng filter đi tiếp (sẽ bị chặn lại nếu api yêu cầu auth)
        log.info("Không tìm thấy token");
        return Mono.empty();
    }
}