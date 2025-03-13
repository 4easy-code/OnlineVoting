package com.auth.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.auth.annotation.RateLimit;
import com.auth.exceptions.RateLimitExceededException;
import com.auth.services.RateLimitService;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
	private Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RateLimitService rateLimitService;

    @Around("@annotation(rateLimit)")
    public Object rateLimitAdvice(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable  {
        logger.info("obtaining the current HttpServletRequest");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String key = rateLimitService.getUserKey(request);

        logger.info("creating a Bucket for this key using parameters from the annotation");
        Bucket bucket = rateLimitService.resolveBucket(
        		key, 
        		rateLimit.capacity(), 
        		rateLimit.refillTokens(), 
        		rateLimit.duration()
        );

        if (bucket.tryConsume(1)) {
        	logger.info("consumed 1 token, proceeding with the method execution");
            return joinPoint.proceed();
        } else {
        	logger.info("rate limit exceeded, throwing exception");
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
        }
    }
}