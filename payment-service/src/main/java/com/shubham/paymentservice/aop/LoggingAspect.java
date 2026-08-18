package com.shubham.paymentservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.shubham.paymentservice.service.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String signature = pjp.getSignature().toShortString();
        try {
            Object result = pjp.proceed();
            log.info("<< {} completed in {} ms", signature, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.warn("<< {} failed in {} ms: {}", signature, System.currentTimeMillis() - start, t.getMessage());
            throw t;
        }
    }
}