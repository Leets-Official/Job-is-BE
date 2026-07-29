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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobSimilarService {

    private final PersonalityTestRepository personalityTestRepository;

    @org.springframework.beans.factory.annotation.Value("${crawler.python-path:python}")
    private String pythonPath;

    @org.springframework.beans.factory.annotation.Value("${matching.database-url:postgresql://postgres:1234@localhost:5432/jobisbe}")
    private String databaseUrl;

    /**
     * 사용자의 성향 퀴즈 결과를 기반으로 맞춤 공고를 추천합니다.
     */
    public SimilarJobsResponseDto getRecommendedJobsByPersonality(Long userId) {
        // 사용자 성향 존재 여부 검증 및 조회
        PersonalityTest personality = personalityTestRepository.findByUserId(userId)
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
        try {
            java.io.File workDir = new java.io.File(System.getProperty("user.dir"));
            java.io.File script = new java.io.File(workDir, "database/matching/engine/retrieve_user_json.py");
            java.io.File stderrFile = java.io.File.createTempFile("retrieve-stderr-", ".log");

            // 동적 페르소나를 인자로 파이썬 스크립트 실행
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath,
                    script.getAbsolutePath(),
                    "--persona",
                    persona
            );

            pb.directory(workDir);
            // stderr 는 파일로 분리 (stdout 은 순수 JSON 이어야 함)
            pb.redirectError(stderrFile);

            // DATABASE_URL 환경 변수 설정 (파이썬이 DB 연결 가능하도록)
            pb.environment().put("DATABASE_URL", databaseUrl);
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();

            String errorOutput = "";
            try {
                errorOutput = java.nio.file.Files.readString(stderrFile.toPath(), StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                // stderr 읽기 실패는 무시
            }
            stderrFile.delete();
            log.info("[Python Retrieve] persona={}, exitCode={}, stdout_len={}, stderr_len={}",
                    persona, exitCode, output.length(), errorOutput.length());

            if (!errorOutput.isBlank()) {
                log.info("[Python Retrieve] stderr:\n{}", errorOutput);
            }

            if (output.length() == 0) {
                log.error("[Python Retrieve] No output from python script for persona: {}", persona);
                throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
            }

            log.debug("[Python Retrieve] stdout: {}", output.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(output.toString());
            JsonNode candidatesNode = rootNode.get("candidates");

            List<SimilarJobItemDto> items = new ArrayList<>();
            if (candidatesNode != null && candidatesNode.isArray()) {
                for (JsonNode node : candidatesNode) {
                    items.add(new SimilarJobItemDto(
                            node.get("external_id").asText(),
                            node.get("position").asText(),
                            node.get("company").asText(),
                            (int) (node.get("score_final").asDouble() * 100),
                            "파이썬 리트리브 기반 추천",
                            List.of("코사인 유사도 일치", "스킬 교집합 매칭"),
                            new CriteriaMatrixDto("✓", "✓", "~", "~", "~", "!")
                    ));
                }
            }
            log.info("[Python Retrieve] Found {} candidates for persona: {}", items.size(), persona);
            return items;
        } catch (Exception e) {
            log.error("[Python Retrieve] Exception occurred for persona: {}", persona, e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
