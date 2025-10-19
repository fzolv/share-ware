package com.fzolv.shareware.hull.configs;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(com.fzolv.shareware..*)")
    public void appPackages() {
    }

    @Around("appPackages() && execution(public * *(..)) && && !@annotation(com.fzolv.shareware.core.annotations.NoLogging)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        log.info("ENTER {} args={}", signature, Arrays.toString(args));
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long tookMs = System.currentTimeMillis() - start;
            log.info("SUCCESS {} took={}ms", signature, tookMs);
            return result;
        } catch (Throwable ex) {
            long tookMs = System.currentTimeMillis() - start;
            log.error("FAIL {} took={}ms error={}: {}", signature, tookMs, ex.getClass().getSimpleName(), ex.getMessage(), ex);
            throw ex;
        }
    }
}


