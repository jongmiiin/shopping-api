package com.skala.shopapi.aop;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ApiLoggingAspect {

    @Around("execution(* com.skala.shopapi.controller..*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        long start = System.currentTimeMillis();

        log.info("[API 요청] {} args={}", method, Arrays.toString(args));
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[API 응답] {} elapsed={}ms result={}", method, elapsed, result);
            return result;
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[API 예외] {} elapsed={}ms error={}", method, elapsed, e.getMessage());
            throw e;
        }
    }
}
