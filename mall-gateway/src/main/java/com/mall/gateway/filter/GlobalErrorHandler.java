package com.mall.gateway.filter;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.error.ErrorCode;

import reactor.core.publisher.Mono;

@Configuration
@Order(-1)
public class GlobalErrorHandler implements ErrorWebExceptionHandler  {
        private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);
        private final ObjectMapper objectMapper = new ObjectMapper();
      
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
       ServerHttpResponse response = exchange.getResponse();
       ErrorCode errorCode = determineErrorCode(ex);
       HttpStatus httpStatus = HttpStatus.valueOf(errorCode.getCode());
       Map<String, Object> errorBody  = new HashMap<>();
       errorBody.put("code", errorCode.getCode());
       errorBody.put("message", errorCode.getMessage());
       errorBody.put("timestamp", System.currentTimeMillis());
       errorBody.put("requestId", MDC.get("traceId"));
       errorBody.put("path", exchange.getRequest().getURI().getPath());

       log.error("Error: {} {} | traceId: {} | code: {} | message: {}",
        exchange.getRequest().getMethod(), 
        exchange.getRequest().getURI().getPath(), 
        MDC.get("requestId"), 
        errorCode.getCode(),
        ex.getMessage());
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (Exception e) {
            log.error("Failed to write error response", e);
            return Mono.error(e);
        }
    }

    private ErrorCode determineErrorCode(Throwable ex) {
        if(ex instanceof ResponseStatusException){
            int statusCode = ((ResponseStatusException) ex).getStatusCode().value();
            switch (statusCode) {
                    case 401: return ErrorCode.UNAUTHORIZED;
                    case 403: return ErrorCode.FORBIDDEN;
                    case 404: return ErrorCode.NOT_FOUND;
                    case 429: return ErrorCode.RATE_LIMITED;
                    default: return ErrorCode.SYSTEM_ERROR;   
            }
                    
        }
        return ErrorCode.SYSTEM_ERROR;   
    }

   
}
