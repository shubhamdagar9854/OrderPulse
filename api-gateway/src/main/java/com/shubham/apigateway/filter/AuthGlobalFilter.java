package com.shubham.apigateway.filter;

import com.shubham.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/users/register",
        "/api/users/login",
        "/"
    );

    private final JwtUtil jwtUtil;

    public AuthGlobalFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        if (PUBLIC_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = jwtUtil.validateToken(token);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userId = jwtUtil.extractUserId(claims).toString();
        String role = jwtUtil.extractRole(claims);

        if (isAdminPath(path, method) && !"ADMIN".equals(role)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
            .request(exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .build())
            .build();

        return chain.filter(mutatedExchange);
    }

    private boolean isAdminPath(String path, String method) {
        if ("GET".equals(method) && path.equals("/api/users")) return true;
        if ("PUT".equals(method) && path.startsWith("/api/users/") && path.endsWith("/role")) return true;
        if ("POST".equals(method) && path.equals("/api/products")) return true;
        if ("PUT".equals(method) && path.startsWith("/api/products/")) return true;
        if ("GET".equals(method) && path.equals("/api/orders")) return true;
        if ("GET".equals(method) && path.startsWith("/api/orders/analytics")) return true;
        if ("PUT".equals(method) && path.matches("/api/orders/\\d+/status")) return true;
        if ("GET".equals(method) && path.equals("/api/payments")) return true;
        return false;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}