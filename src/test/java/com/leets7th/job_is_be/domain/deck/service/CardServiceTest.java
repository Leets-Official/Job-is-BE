package com.leets7th.job_is_be.domain.deck.service;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.JobPosting;
import com.leets7th.job_is_be.domain.job.repository.JobPostingRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private DeckRepository deckRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private JobPostingRepository jobPostingRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void 덱이_없으면_예외를_던진다() {
        when(deckRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.getDeckCards(1L))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void 덱의_카드_목록을_추천_이유와_함께_반환한다() {
        when(deckRepository.existsById(1L)).thenReturn(true);

        Job job = Job.builder()
                .title("백엔드 엔지니어 채용")
                .careerLevel("신입")
                .source("wanted")
                .externalId(10L)
                .build();
        Card card = Card.builder()
                .job(job)
                .position(1)
                .fitScore(new BigDecimal("92.50"))
                .reason("최근 관심 직무와 일치합니다")
                .summary("백엔드, 3년 이상, 서울")
                .build();
        when(cardRepository.findByDeckId(1L)).thenReturn(List.of(card));

        JobPosting posting = JobPosting.builder()
                .source("wanted")
                .externalId(10L)
                .skillTags(List.of("Java", "Spring"))
                .build();
        when(jobPostingRepository.findFirstBySourceAndExternalIdOrderByCollectedAtDesc("wanted", 10L))
                .thenReturn(Optional.of(posting));

        List<CardResponse> response = cardService.getDeckCards(1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).jobTitle()).isEqualTo("백엔드 엔지니어 채용");
        assertThat(response.get(0).reason()).isEqualTo("최근 관심 직무와 일치합니다");
        assertThat(response.get(0).summary()).isEqualTo("백엔드, 3년 이상, 서울");
        assertThat(response.get(0).techStack()).containsExactly("Java", "Spring");
        assertThat(response.get(0).tags()).containsExactly("신입", "연봉 비공개");
    }
}
