package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.JobLinkValidateResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobLinkValidationService {

    private final RestClient restClient;
    private final JobRepository jobRepository;

    public JobLinkValidateResponse validateJobLink(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.JOB_NOT_FOUND));

        if (job.getStatus() != JobStatus.ACTIVE) {
            return JobLinkValidateResponse.of(false, null, "마감되거나 삭제된 공고입니다.");
        }

        String url = job.getSourceUrl();

        if (url == null || url.isBlank()) {
            return JobLinkValidateResponse.of(false, null, "등록된 원문 링크가 없습니다.");
        }

        return validateLink(url);
    }

    public JobLinkValidateResponse validateLink(String url) {
        // 1차 시도: HEAD 요청으로 네트워크 대역폭 절약
        JobLinkValidateResponse headResult = executeRequest(url, HttpMethod.HEAD);
        if (headResult.isValid()) {
            return headResult;
        }

        // 2차 시도: HEAD 차단(405, 403 등) 또는 실패 시 GET 요청 Fallback
        log.info("HEAD 요청 실패로 GET 요청 재시도를 진행합니다. URL: {}", url);
        return executeRequest(url, HttpMethod.GET);
    }

    private JobLinkValidateResponse executeRequest(String url, HttpMethod method) {
        try {
            HttpStatusCode statusCode = restClient.method(method)
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode();

            boolean isValid = statusCode.is2xxSuccessful() || statusCode.is3xxRedirection();
            String message = isValid ? "정상 접근 가능한 링크입니다." : "접속할 수 없는 링크입니다. (상태 코드: " + statusCode.value() + ")";

            return JobLinkValidateResponse.of(isValid, statusCode.value(), message);

        } catch (HttpStatusCodeException e) {
            // 4xx, 5xx HTTP 에러 응답 수신 시 예외를 던지지 않고 결과를 DTO로 처리
            int code = e.getStatusCode().value();
            return JobLinkValidateResponse.of(false, code, "HTTP 오류 응답 수신 (상태 코드: " + code + ")");
        } catch (Exception e) {
            // ConnectTimeoutException, UnknownHostException 등 연결/타임아웃 에러 처리
            log.warn("링크 유효성 검증 실패 (Method: {}, URL: {}): {}", method, url, e.getMessage());
            return JobLinkValidateResponse.of(false, null, "연결 실패 또는 타임아웃이 발생했습니다.");
        }
    }
}
