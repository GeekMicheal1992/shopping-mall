package com.mall.gateway.filter;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.mall.common.error.ErrorCode;
import com.mall.common.security.TokenValidator;
import com.mall.gateway.config.WhitelistMatcher;
import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

/*
    作用：网关前置拦截。
    流程是：白名单放行 -> 非白名单检查 Authorization/Bearer -> 不通过返回 401。
    */
@Component
//GatewayFilterChain、GlobalFilter：定义这是网关过滤器，Ordered：定义过滤器执行顺序，数值越小优先级越高。
public class GatewayAuthFilter  implements GlobalFilter, Ordered {

    private final WhitelistMatcher whitelistMatcher;
    private static final Logger log = LoggerFactory.getLogger(GatewayAuthFilter.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenValidator tokenValidator;
    public GatewayAuthFilter(WhitelistMatcher whitelistMatcher, RedisTemplate<String, String> redisTemplate, TokenValidator tokenValidator) {
        this.whitelistMatcher = whitelistMatcher;
        this.redisTemplate = redisTemplate;
        this.tokenValidator = tokenValidator;
    }

    @Override
    //ServerWebExchange 包含了请求和响应的信息，GatewayFilterChain 用来继续调用下一个过滤器。
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (whitelistMatcher.isWhitelisted(path)) {
            return chain.filter(exchange);
        }
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return writeUnauthorized(exchange.getResponse(), exchange.getRequest().getId(),  ErrorCode.UNAUTHORIZED);

        }
        String token = authHeader.substring(7);
        Claims claims = tokenValidator.parseToken(token);
        if (claims == null) {
            return writeUnauthorized(exchange.getResponse(), exchange.getRequest().getId(), ErrorCode.TOKEN_INVALID);
        }
        String jti = claims.get("jti", String.class);
        String  redisKey = "auth:token:blacklist:" + jti;
        if(redisTemplate.hasKey(redisKey)){
            return writeUnauthorized(exchange.getResponse(), exchange.getRequest().getId(), ErrorCode.TOKEN_INVALID);
        }
        Integer userIdInt  = claims.get("uid", Integer.class);
        String userName = claims.get("sub", String.class);

        String UserId = String.valueOf(userIdInt);
        String role = claims.get("role", String.class);
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", UserId)
                .header("X-User-Name", userName)
                .header("X-User-Role", role)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        return chain.filter(mutatedExchange);
    }
    //ServerHttpResponse 写响应
    private Mono<Void> writeUnauthorized(ServerHttpResponse response, String requestId, ErrorCode errorCode) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"code\":%d,\"message\":\"%s\",\"requestId\":\"%s\",\"timestamp\":%d}", 
        errorCode.getCode(), 
        errorCode.getMessage(), 
        requestId,
        System.currentTimeMillis()
        );

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    
    @Override
    public int getOrder() {
        return -100; 
    }

    // 新增：403 无权限响应
    private Mono<Void> writeForbidden(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"code\":403,\"message\":\"%s\",\"timestamp\":%d}", 
            message, System.currentTimeMillis());
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
    
}
