package com.moim.api.repository.log;

import com.moim.api.domain.log.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApiLogRepository
        extends JpaRepository<ApiLog, Long>, JpaSpecificationExecutor<ApiLog> {
}
