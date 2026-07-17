package com.leets7th.job_is_be.domain.deck.service;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 추천 파이프라인 — 오늘의 Deck을 만들고 Card로 채운다.
 * TODO: 실제 추천엔진(recall/precision, 사용자 선호 매칭, fit score 산정)은 아직 없음.
 *       그 전까지는 지원 가능한 최신 공고를 그대로 채우는 임시 로직으로 대체.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationService {

    private static final int DECK_SIZE = 5;

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;

    // 스웨거로 파이프라인 동작을 확인하기 위한 수동 트리거 — 오늘 덱이 없으면 생성하고, 있으면 그대로 반환
    public List<CardResponse> generateTodayDeck(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        LocalDate today = LocalDate.now();
        Deck deck = deckRepository.findByUserIdAndDeckDate(userId, today)
                .orElseGet(() -> deckRepository.save(Deck.builder().user(user).deckDate(today).build()));

        if (cardRepository.findByDeckId(deck.getId()).isEmpty()) {
            fillWithPlaceholderCards(deck);
        }

        return cardService.getDeckCards(deck.getId());
    }

    private void fillWithPlaceholderCards(Deck deck) {
        List<Job> jobs = jobRepository.findApplicableJobs(
                JobStatus.ACTIVE, OffsetDateTime.now(), PageRequest.of(0, DECK_SIZE));

        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < jobs.size(); i++) {
            cards.add(Card.builder()
                    .deck(deck)
                    .job(jobs.get(i))
                    .position(i + 1)
                    .reason("TODO: 추천엔진 연동 전 임시로 채워진 카드입니다")
                    .build());
        }
        cardRepository.saveAll(cards);
    }
}
