package com.mall.gateway.config;

import java.util.List;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Configuration
public class GatewayRateLimitConfig {
    /*
        提供 ipKeyResolver Bean，给 yml 里的 RequestRateLimiter 使用，决定“按谁限流”（你现在按 IP）。
     */
    @Bean("ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(resolveClientIp(exchange));
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        List<String> forwardedFor = exchange.getRequest().getHeaders().get("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            String first = forwardedFor.get(0);
            if (first != null && !first.isBlank()) {
                return first.split(",")[0].trim();
            }
        }

        if (exchange.getRequest().getRemoteAddress() != null
                && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    @Bean("userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> {
           String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
           if(userId != null && !userId.isBlank()) {
            return Mono.just("user:" + userId);
           }
           else {
            return Mono.just("ip:" + resolveClientIp(exchange));
           }
        };
    }

     @Bean("apiKeyResolver")
    public KeyResolver apiKeyResolver() {
        return exchange -> {
              String path = exchange.getRequest().getURI().getPath();
              return Mono.just("api:" + path);
        };
    }
}
