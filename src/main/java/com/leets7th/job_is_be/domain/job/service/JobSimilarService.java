package com.leets7th.job_is_be.domain.job.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets7th.job_is_be.domain.job.dto.CriteriaMatrixDto;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobItemDto;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobsResponseDto;
import com.leets7th.job_is_be.domain.personality.entity.PersonalityTest;
import com.leets7th.job_is_be.domain.personality.repository.PersonalityTestRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobSimilarService {

    // 임베딩 모델 최초 로딩 + 신규 공고 벡터 계산까지 감안한 여유값
    private static final long PYTHON_TIMEOUT_SECONDS = 120;

    private final PersonalityTestRepository personalityTestRepository;

    @org.springframework.beans.factory.annotation.Value("${crawler.python-path:python}")
    private String pythonPath;

    @org.springframework.beans.factory.annotation.Value("${matching.database-url:}")
    private String databaseUrl;

    /**
     * 사용자의 성향 퀴즈 결과를 기반으로 맞춤 공고를 추천합니다.
     */
    public SimilarJobsResponseDto getRecommendedJobsByPersonality(Long userId) {
        // 사용자 성향 존재 여부 검증 및 조회
        PersonalityTest personality = personalityTestRepository
                .findFirstByUserIdAndCompletedTrueOrderByStartedAtDesc(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PERSONALITY_NOT_FOUND));

        // 엔티티에 정의된 페르소나 필드 메서드명으로 변경 필요 (예: getPersona(), getExtRef() 등)
        String persona = personality.getResultType() != null
                ? personality.getResultType().name().toLowerCase()
                : "p2-senior-backend";

        // 파이썬 리트리브 스크립트 실행 및 결과 파싱
        List<SimilarJobItemDto> items = executePythonRetrieve(persona);

        return SimilarJobsResponseDto.of(String.valueOf(userId), items);
    }

    private List<SimilarJobItemDto> executePythonRetrieve(String persona) {

        java.io.File stderrFile = null;
        java.io.File stdoutFile = null;
        try {
            java.io.File workDir = new java.io.File(System.getProperty("user.dir"));
            java.io.File script = new java.io.File(workDir, "database/matching/engine/retrieve_user_json.py");
            stderrFile = java.io.File.createTempFile("retrieve-stderr-", ".log");
            stdoutFile = java.io.File.createTempFile("retrieve-stdout-", ".json");

            // 동적 페르소나를 인자로 파이썬 스크립트 실행
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath,
                    script.getAbsolutePath(),
                    "--persona",
                    persona
            );

            pb.directory(workDir);
            // stdout/stderr 를 모두 파일로 받는다.
            // 파이프로 받으면서 waitFor 로 먼저 대기하면, 출력이 파이프 버퍼(윈도우 기본 4KB)를 넘는 순간
            // 파이썬은 "자바가 읽어가길", 자바는 "파이썬이 끝나길" 기다리는 교착이 발생한다.
            pb.redirectOutput(stdoutFile);
            pb.redirectError(stderrFile);

            // DATABASE_URL 환경 변수 설정 (파이썬이 DB 연결 가능하도록)
            pb.environment().put("DATABASE_URL", databaseUrl);
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            Process process = pb.start();

            // 임베딩 모델 최초 로딩이 오래 걸릴 수 있어 여유를 둔다
            boolean completed = process.waitFor(PYTHON_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                log.error("[Python Retrieve] {}초 안에 끝나지 않아 강제 종료했습니다. persona={}",
                        PYTHON_TIMEOUT_SECONDS, persona);
                throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
            }

            int exitCode = process.exitValue();

            StringBuilder output = new StringBuilder(readFileQuietly(stdoutFile));
            String errorOutput = readFileQuietly(stderrFile);
            log.info("[Python Retrieve] persona={}, exitCode={}, stdout_len={}, stderr_len={}",
                    persona, exitCode, output.length(), errorOutput.length());

            if (!errorOutput.isBlank()) {
                log.info("[Python Retrieve] stderr:\n{}", errorOutput);
            }

            if (output.length() == 0) {
                // 스크립트가 stdout 에 아무것도 남기지 못하고 죽은 경우 — 원인은 stderr 에만 있다
                log.error("[Python Retrieve] 파이썬이 출력을 내지 못했습니다. persona={}, exitCode={}, python={}, script={}\n--- stderr ---\n{}",
                        persona, exitCode, pythonPath, script.getAbsolutePath(),
                        errorOutput.isBlank() ? "(stderr 없음 — 실행 파일 경로를 확인하세요)" : errorOutput);
                throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
            }

            log.debug("[Python Retrieve] stdout: {}", output.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(output.toString());
            JsonNode candidatesNode = rootNode.get("candidates");

            JsonNode personaNode = rootNode.get("persona");

            List<SimilarJobItemDto> items = new ArrayList<>();
            if (candidatesNode != null && candidatesNode.isArray()) {
                for (JsonNode node : candidatesNode) {
                    double scoreRaw = node.get("score_final").asDouble() * 100;
                    int fitScore = (int) Math.max(0, Math.min(100, scoreRaw));

                    List<String> fitPoints = buildFitPoints(node, personaNode);

                    items.add(new SimilarJobItemDto(
                            node.get("external_id").asText(),
                            node.get("position").asText(),
                            node.get("company").asText(),
                            fitScore,
                            buildReason(fitPoints),
                            fitPoints,
                            buildCriteriaMatrix(node, personaNode)
                    ));
                }
            }
            log.info("[Python Retrieve] Found {} candidates for persona: {}", items.size(), persona);
            return items;
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Python Retrieve] 실행/파싱 실패 persona={}", persona, e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // 예외가 나도 임시 파일은 반드시 정리한다
            deleteQuietly(stdoutFile);
            deleteQuietly(stderrFile);
        }
    }

    /**
     * 확인 가능한 신호만 근거로 남긴다. 파이썬 출력에 없는 축(직무·경력)은 주장하지 않는다.
     */
    private List<String> buildFitPoints(JsonNode candidate, JsonNode persona) {
        List<String> points = new ArrayList<>();

        List<String> matchedSkills = matchedSkills(candidate, persona);
        if (!matchedSkills.isEmpty()) {
            points.add(String.join(", ", matchedSkills) + " 스킬이 겹칩니다");
        }
        if (isLocationMatched(candidate, persona)) {
            points.add("선호 지역과 일치합니다");
        }
        if (isCompanySizeMatched(candidate, persona)) {
            points.add(text(candidate, "company_type") + " 규모를 선호합니다");
        }

        double cosine = candidate.path("score_cosine").asDouble();
        if (cosine >= 0.6) {
            points.add("공고 내용이 관심 직무와 유사합니다");
        }
        return points;
    }

    private String buildReason(List<String> fitPoints) {
        // 근거가 하나도 없으면 단정하지 않는다(§9 추정 금지)
        return fitPoints.isEmpty() ? "관심 직무와 일부 유사한 공고입니다" : fitPoints.get(0);
    }

    /**
     * 확인 가능한 축만 판정하고, 파이썬 출력에 근거가 없는 축은 미확인(~)으로 둔다.
     * 급여는 데이터가 없어 항상 "!"(화면설계서 §2.3).
     */
    private CriteriaMatrixDto buildCriteriaMatrix(JsonNode candidate, JsonNode persona) {
        String skills = matchedSkills(candidate, persona).isEmpty() ? "~" : "✓";
        String location = isLocationMatched(candidate, persona) ? "✓" : "~";
        String preference = isCompanySizeMatched(candidate, persona) ? "✓" : "~";
        String jobType = candidate.path("score_cosine").asDouble() >= 0.6 ? "✓" : "~";

        // 경력은 파이썬 응답에 담기지 않아 판정 불가
        return new CriteriaMatrixDto(jobType, "~", location, skills, preference, "!");
    }

    private List<String> matchedSkills(JsonNode candidate, JsonNode persona) {
        List<String> personaSkills = textList(persona, "skills");
        if (personaSkills.isEmpty()) {
            return List.of();
        }
        return textList(candidate, "skill_tags").stream()
                .filter(tag -> personaSkills.stream().anyMatch(tag::equalsIgnoreCase))
                .toList();
    }

    private boolean isLocationMatched(JsonNode candidate, JsonNode persona) {
        String location = text(candidate, "location_full");
        if (location.isBlank()) {
            return false;
        }
        return textList(persona, "locations").stream().anyMatch(location::contains);
    }

    private boolean isCompanySizeMatched(JsonNode candidate, JsonNode persona) {
        String companyType = text(candidate, "company_type");
        if (companyType.isBlank()) {
            return false;
        }
        return textList(persona, "company_size_pref").contains(companyType);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private List<String> textList(JsonNode node, String field) {
        JsonNode array = node == null ? null : node.get(field);
        if (array == null || !array.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        array.forEach(element -> values.add(element.asText()));
        return values;
    }

    private String readFileQuietly(java.io.File file) {
        try {
            return java.nio.file.Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private void deleteQuietly(java.io.File file) {
        if (file != null && file.exists() && !file.delete()) {
            log.warn("[Python Retrieve] 임시 파일 삭제 실패: {}", file.getAbsolutePath());
        }
    }
}
