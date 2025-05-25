package com.github.timebetov.microblog.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
@Slf4j
public class LoggerAspect {

    @Around("execution(* com.github.timebetov.microblog.services.*.*(..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toString();
        log.info(methodName + " method execution started.");
        Instant start = Instant.now();
        Object returnObj = joinPoint.proceed();
        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        log.info(methodName + " method execution completed in " + duration + " ms.");
        return returnObj;
    }
}
