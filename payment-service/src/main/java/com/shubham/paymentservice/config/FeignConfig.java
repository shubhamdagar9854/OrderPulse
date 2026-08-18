package com.shubham.paymentservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor internalAuthInterceptor() {
        return template -> {
            template.header("X-User-Role", "ADMIN");
            template.header("X-User-Id", "0");
        };
    }
}