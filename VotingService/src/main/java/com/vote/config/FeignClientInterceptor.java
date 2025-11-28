package com.vote.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignClientInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                logger.info("FeignClientInterceptor: Extracted JWT Token - {}", authHeader);
                template.header("Authorization", authHeader); // Attach token to Feign request
                logger.info("FeignClientInterceptor: JWT Token successfully added to Feign request.");
            } else {
                logger.warn("FeignClientInterceptor: No Authorization header found in request.");
            }
        } else {
            logger.error("FeignClientInterceptor: No current HTTP request found (RequestContextHolder is null).");
        }
    }
}

