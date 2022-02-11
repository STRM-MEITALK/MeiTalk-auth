package com.meitalk.auth.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAOP {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(com.meitalk.auth.controller..*)")
    public void onRequest() {

    }

    @Around("com.meitalk.auth.config.LoggingAOP.onRequest()")
    public Object requestLogging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        long start = System.currentTimeMillis();
        try {
            return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        } finally {
            long end = System.currentTimeMillis();
            if (request.getMethod().equalsIgnoreCase("POST") && request.getContentType().equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)) {
                logger.info("Request: {} {}: {} ({}ms) (ip : {})", request.getMethod(), request.getRequestURL(), JsonUtils.readJSONStringFromRequestBody(request), end - start, getIp(request));
            } else {
                logger.info("Request: {} {}: {} ({}ms (ip : {}))", request.getMethod(), request.getRequestURL(), paramMapToString(request.getParameterMap()), end - start, getIp(request));
            }
        }
    }

    private String paramMapToString(Map<String, String[]> paraStringMap) {
        return paraStringMap.entrySet().stream()
                .map(entry -> String.format("%s : %s", entry.getKey(), Arrays.toString(entry.getValue())))
                .collect(Collectors.joining(", "));
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }


}
