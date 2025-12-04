package com.moim.api.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

        @GetMapping("/health")
        public ResponseEntity<HealthResponse> health() {
                HealthResponse response = new HealthResponse(
                                "OK",
                                "Moim API",
                                "1.0.0",
                                LocalDateTime.now());

                return ResponseEntity.ok(response);
        }

        /**
         * DTO used by Swagger schema example and runtime response.
         */
        private record HealthResponse(
                        String status,
                        String service,
                        String version,
                        LocalDateTime timestamp) {
        }
}
