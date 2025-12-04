package com.moim.api.dto.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moim.api.domain.log.ApiLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ApiLogCreateRequest(
        @NotBlank(message = "HTTP 메서드는 필수입니다.") @Size(max = 10,
                message = "HTTP 메서드는 10자를 넘을 수 없습니다.") String httpMethod,

        @NotBlank(message = "요청 URI는 필수입니다.") @Size(max = 255,
                message = "요청 URI는 255자를 넘을 수 없습니다.") String requestUri,

        @NotNull(message = "응답 상태 코드는 필수입니다.") Integer responseStatus,

        Long durationMs,

        @Size(max = 64, message = "IP는 64자를 넘을 수 없습니다.") String clientIp,

        @Size(max = 255, message = "User-Agent는 255자를 넘을 수 없습니다.") String userAgent,

        @Size(max = 2000, message = "요청 바디는 2000자를 넘을 수 없습니다.") String requestBody,

        @Size(max = 2000, message = "응답 바디는 2000자를 넘을 수 없습니다.") String responseBody,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime occurredAt) {
    public ApiLog toEntity() {
        return ApiLog.builder().httpMethod(httpMethod.toUpperCase()).requestUri(requestUri)
                .responseStatus(responseStatus).durationMs(durationMs).clientIp(clientIp)
                .userAgent(userAgent).requestBody(requestBody).responseBody(responseBody)
                .occurredAt(occurredAt).build();
    }
}
