package com.moim.api.dto.log;

import com.moim.api.domain.log.ApiLog;
import java.time.LocalDateTime;

public record ApiLogResponse(Long id, String httpMethod, String requestUri, Integer responseStatus,
        Long durationMs, String clientIp, String userAgent, String requestBody, String responseBody,
        LocalDateTime occurredAt) {
    public static ApiLogResponse from(ApiLog apiLog) {
        return new ApiLogResponse(apiLog.getId(), apiLog.getHttpMethod(), apiLog.getRequestUri(),
                apiLog.getResponseStatus(), apiLog.getDurationMs(), apiLog.getClientIp(),
                apiLog.getUserAgent(), apiLog.getRequestBody(), apiLog.getResponseBody(),
                apiLog.getOccurredAt());
    }
}
