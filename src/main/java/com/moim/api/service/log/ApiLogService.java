package com.moim.api.service.log;

import com.moim.api.domain.log.ApiLog;
import com.moim.api.dto.log.ApiLogCreateRequest;
import com.moim.api.dto.log.ApiLogResponse;
import com.moim.api.dto.log.ApiLogSearchCondition;
import com.moim.api.repository.log.ApiLogRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiLogService {

    private final ApiLogRepository apiLogRepository;

    @Transactional
    public ApiLogResponse save(ApiLogCreateRequest request) {
        ApiLog entity =
                Objects.requireNonNull(request.toEntity(), "apiLog entity must not be null");
        ApiLog saved = apiLogRepository.save(entity);
        return ApiLogResponse.from(saved);
    }

    @Transactional
    public void record(ApiLog apiLog) {
        apiLogRepository.save(Objects.requireNonNull(apiLog, "apiLog must not be null"));
    }

    public List<ApiLogResponse> search(ApiLogSearchCondition condition) {
        Specification<ApiLog> specification = buildSpecification(condition);
        PageRequest pageRequest = PageRequest.of(0, condition.sanitizedLimit(),
                Sort.by(Sort.Direction.DESC, "occurredAt"));

        Page<ApiLog> page = apiLogRepository.findAll(specification, pageRequest);
        return page.stream().map(ApiLogResponse::from).toList();
    }

    private Specification<ApiLog> buildSpecification(ApiLogSearchCondition condition) {
        Specification<ApiLog> spec = (root, query, cb) -> cb.conjunction();

        if (condition.from() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"),
                    condition.from()));
        }
        if (condition.to() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"),
                    condition.to()));
        }
        if (StringUtils.hasText(condition.httpMethod())) {
            String method = condition.httpMethod().trim().toUpperCase();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("httpMethod"), method));
        }
        if (StringUtils.hasText(condition.requestUriKeyword())) {
            String keyword = "%" + condition.requestUriKeyword().trim() + "%";
            spec = spec.and((root, query, cb) -> cb.like(root.get("requestUri"), keyword));
        }
        if (condition.responseStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("responseStatus"),
                    condition.responseStatus()));
        }

        return spec;
    }
}
