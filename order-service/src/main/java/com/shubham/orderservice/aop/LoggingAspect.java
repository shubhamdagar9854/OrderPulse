package com.shubham.orderservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.shubham.orderservice.service.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String signature = pjp.getSignature().toShortString();
        log.info(">> {} args={}", signature, truncateArgs(pjp.getArgs()));
        try {
            Object result = pjp.proceed();
            log.info("<< {} completed in {} ms", signature, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.warn("<< {} failed in {} ms: {}", signature, System.currentTimeMillis() - start, t.getMessage());
            throw t;
        }
    }

    private String truncateArgs(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        return Arrays.toString(Arrays.stream(args)
                .map(a -> a == null ? "null" : String.valueOf(a).length() > 120 ? String.valueOf(a).substring(0, 120) + "..." : String.valueOf(a))
                .toArray());
    }
}