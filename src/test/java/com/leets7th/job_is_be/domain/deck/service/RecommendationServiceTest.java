package com.leets7th.job_is_be.domain.deck.service;

import com.leets7th.job_is_be.domain.deck.dto.CardResponse;
import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.deck.repository.CardRepository;
import com.leets7th.job_is_be.domain.deck.repository.DeckRepository;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobItemDto;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobsResponseDto;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.service.JobSimilarService;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private DeckRepository deckRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardService cardService;
    @Mock
    private JobSimilarService jobSimilarService;

    @InjectMocks
    private RecommendationService recommendationService;

    private static SimilarJobItemDto item(String jobId, int fitScore) {
        return new SimilarJobItemDto(jobId, "백엔드 엔지니어", "래브라도랩스", fitScore,
                "성향과 기술 스택이 맞습니다", List.of("코사인 유사도 일치"), null);
    }

    @Test
    void 사용자가_없으면_예외를_던진다() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recommendationService.generateTodayDeck(1L))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void 오늘_덱이_없으면_추천엔진_결과로_카드를_채운다() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Deck savedDeck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        when(deckRepository.findByUserIdAndDeckDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(deckRepository.save(any(Deck.class))).thenReturn(savedDeck);
        when(cardRepository.findByDeckId(any())).thenReturn(List.of());

        when(jobSimilarService.getRecommendedJobsByPersonality(1L))
                .thenReturn(SimilarJobsResponseDto.of("1", List.of(item("111", 84))));

        Job job = Job.builder().title("백엔드 엔지니어").externalId(111L).employmentType("정규직").build();
        when(jobRepository.findByExternalIdIn(List.of(111L))).thenReturn(List.of(job));

        CardResponse response = new CardResponse(1L, 2L, null, null, "백엔드 엔지니어", "성향과 기술 스택이 맞습니다",
                BigDecimal.valueOf(84), List.of(), null, null, List.of(), null, "요약", 1, null);
        when(cardService.getDeckCards(any(), any())).thenReturn(List.of(response));

        List<CardResponse> result = recommendationService.generateTodayDeck(1L);

        ArgumentCaptor<List<Card>> cardsCaptor = ArgumentCaptor.forClass(List.class);
        verify(cardRepository).saveAll(cardsCaptor.capture());

        List<Card> saved = cardsCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getPosition()).isEqualTo(1);
        assertThat(saved.get(0).getFitScore()).isEqualByComparingTo(BigDecimal.valueOf(84));
        assertThat(saved.get(0).getReason()).isEqualTo("성향과 기술 스택이 맞습니다");
        assertThat(saved.get(0).getSummary()).contains("정규직").contains("상시");
        assertThat(result).containsExactly(response);
    }

    @Test
    void 성향_퀴즈가_없으면_카드를_만들지_않고_온보딩_미완으로_기록한다() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Deck savedDeck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        when(deckRepository.findByUserIdAndDeckDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(deckRepository.save(any(Deck.class))).thenReturn(savedDeck);
        when(cardRepository.findByDeckId(any())).thenReturn(List.of());
        when(cardService.getDeckCards(any(), any())).thenReturn(List.of());

        when(jobSimilarService.getRecommendedJobsByPersonality(1L))
                .thenThrow(new GeneralException(ErrorStatus.PERSONALITY_NOT_FOUND));

        recommendationService.generateTodayDeck(1L);

        verify(cardRepository, never()).saveAll(any());
        assertThat(savedDeck.getEmptyReason()).isEqualTo("onboarding_incomplete");
    }

    @Test
    void 추천_후보가_0건이면_후보_부족으로_기록한다() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Deck savedDeck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        when(deckRepository.findByUserIdAndDeckDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(deckRepository.save(any(Deck.class))).thenReturn(savedDeck);
        when(cardRepository.findByDeckId(any())).thenReturn(List.of());
        when(cardService.getDeckCards(any(), any())).thenReturn(List.of());

        when(jobSimilarService.getRecommendedJobsByPersonality(1L))
                .thenReturn(SimilarJobsResponseDto.of("1", List.of()));

        recommendationService.generateTodayDeck(1L);

        verify(cardRepository, never()).saveAll(any());
        assertThat(savedDeck.getEmptyReason()).isEqualTo("no_candidates");
    }

    @Test
    void 오늘_덱에_이미_카드가_있으면_다시_채우지_않는다() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Deck deck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        when(deckRepository.findByUserIdAndDeckDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(deck));

        Card existingCard = Card.builder().deck(deck).position(1).build();
        when(cardRepository.findByDeckId(any())).thenReturn(List.of(existingCard));
        when(cardService.getDeckCards(any(), any())).thenReturn(List.of());

        recommendationService.generateTodayDeck(1L);

        verify(jobSimilarService, never()).getRecommendedJobsByPersonality(any());
        verify(cardRepository, never()).saveAll(any());
    }
}
