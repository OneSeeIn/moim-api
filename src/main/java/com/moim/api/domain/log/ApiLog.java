package com.moim.api.domain.log;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "api_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String httpMethod;

    @Column(nullable = false, length = 255)
    private String requestUri;

    @Column(nullable = false)
    private Integer responseStatus;

    @Column
    private Long durationMs;

    @Column(length = 64)
    private String clientIp;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 2000)
    private String requestBody;

    @Column(length = 2000)
    private String responseBody;

    @Column(nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @Builder
    private ApiLog(Long id, String httpMethod, String requestUri, Integer responseStatus,
            Long durationMs, String clientIp, String userAgent, String requestBody,
            String responseBody, LocalDateTime occurredAt) {
        this.id = id;
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
        this.responseStatus = responseStatus;
        this.durationMs = durationMs;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.occurredAt = occurredAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.occurredAt == null) {
            this.occurredAt = LocalDateTime.now();
        }
    }
}
