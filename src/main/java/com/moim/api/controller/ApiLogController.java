package com.moim.api.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.moim.api.dto.log.ApiLogResponse;
import com.moim.api.dto.log.ApiLogSearchCondition;
import com.moim.api.service.log.ApiLogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class ApiLogController {

    private final ApiLogService apiLogService;

    @GetMapping
    public ResponseEntity<List<ApiLogResponse>> listLogs(
            @RequestParam(name = "from", required = false) @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @RequestParam(name = "to", required = false) @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @RequestParam(name = "httpMethod", required = false) String httpMethod,

            @RequestParam(name = "requestUriKeyword", required = false) String requestUriKeyword,

            @RequestParam(name = "responseStatus", required = false) Integer responseStatus,

            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(200) int limit) {
        ApiLogSearchCondition condition = new ApiLogSearchCondition(from, to, httpMethod,
                requestUriKeyword, responseStatus, limit);

        List<ApiLogResponse> responses = apiLogService.search(condition);
        return ResponseEntity.ok(responses);
    }
}
