package com.moim.api.logging;

import com.moim.api.domain.log.ApiLog;
import com.moim.api.service.log.ApiLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@RequiredArgsConstructor
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_BODY_LENGTH = 2000;
    private final ApiLogService apiLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            persistLog(requestWrapper, responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void persistLog(ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response, long duration) {
        try {
            ApiLog apiLog = ApiLog.builder().httpMethod(request.getMethod())
                    .requestUri(request.getRequestURI()).responseStatus(response.getStatus())
                    .durationMs(duration).clientIp(resolveClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .requestBody(extractBody(request.getContentAsByteArray()))
                    .responseBody(extractBody(response.getContentAsByteArray()))
                    .occurredAt(LocalDateTime.now()).build();

            apiLogService.record(apiLog);
        } catch (Exception e) {
            log.warn("Failed to persist API log", e);
        }
    }

    private String extractBody(byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }
        String body = new String(content, StandardCharsets.UTF_8);
        if (body.length() <= MAX_BODY_LENGTH) {
            return body;
        }
        return body.substring(0, MAX_BODY_LENGTH);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
