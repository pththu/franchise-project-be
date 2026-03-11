package com.franchiseproject.identityaccessservice.security;

import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final RoleRepository roleRepository;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        String requestUrl = request.getRequestURI();
        String requestMethod = request.getMethod();

        Authentication auth = authenticationSupplier.get();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return new AuthorizationDecision(false);
        }

        String roleName = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        if (roleName.isEmpty()) {
            return new AuthorizationDecision(false);
        }

        Role role = roleRepository.findByName(roleName).orElse(null);
        if (role == null || role.getPermissions() == null || role.getPermissions().isEmpty()) {
            return new AuthorizationDecision(false);
        }

        boolean isAllowed = role.getPermissions().stream().anyMatch(permission -> {
            boolean methodMatch = "ANY".equalsIgnoreCase(permission.getHttpMethod())
                    || requestMethod.equalsIgnoreCase(permission.getHttpMethod());
            boolean urlMatch = antPathMatcher.match(permission.getApi(), requestUrl);

            return methodMatch && urlMatch;
        });

        if (isAllowed) {
            log.info("Access GRANTED: Role [{}] truy cập [{}] {}", roleName, requestMethod, requestUrl);
        } else {
            log.warn("Access DENIED: Role [{}] bị chặn khi cố truy cập [{}] {}", roleName, requestMethod, requestUrl);
        }

        return new AuthorizationDecision(isAllowed);
    }
}