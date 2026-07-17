package com.leets7th.job_is_be.domain.deck.service;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.JobPostingRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final JobPostingRepository jobPostingRepository;

    // 추천 파이프라인(recall/precision)이 채운 덱의 카드 목록을 그대로 조회
    public List<CardResponse> getDeckCards(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            throw new GeneralException(ErrorStatus.DECK_NOT_FOUND);
        }

        return cardRepository.findByDeckId(deckId).stream()
                .map(card -> CardResponse.from(card, resolveTechStack(card.getJob())))
                .toList();
    }

    // 기술 스택은 서빙용 jobs가 아닌 원문 job_postings의 skill_tags에만 있어 (source, externalId)로 역참조
    private List<String> resolveTechStack(Job job) {
        if (job == null || job.getSource() == null || job.getExternalId() == null) {
            return List.of();
        }
        return jobPostingRepository
                .findFirstBySourceAndExternalIdOrderByCollectedAtDesc(job.getSource(), job.getExternalId())
                .map(posting -> posting.getSkillTags() != null ? posting.getSkillTags() : List.<String>of())
                .orElse(List.of());
    }
}
