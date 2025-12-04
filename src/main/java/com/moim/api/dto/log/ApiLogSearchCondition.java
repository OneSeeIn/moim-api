package com.moim.api.dto.log;

import java.time.LocalDateTime;

public record ApiLogSearchCondition(LocalDateTime from, LocalDateTime to, String httpMethod,
        String requestUriKeyword, Integer responseStatus, int limit) {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    public int sanitizedLimit() {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
