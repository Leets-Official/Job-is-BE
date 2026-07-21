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
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void 사용자가_없으면_예외를_던진다() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recommendationService.generateTodayDeck(1L))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void 오늘_덱이_없으면_새로_만들고_지원가능한_공고로_카드를_채운다() {
        User user = User.builder().socialId("s").socialType(SocialType.KAKAO).email("a@a.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Deck savedDeck = Deck.builder().user(user).deckDate(LocalDate.now()).build();
        when(deckRepository.findByUserIdAndDeckDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(deckRepository.save(any(Deck.class))).thenReturn(savedDeck);
        when(cardRepository.findByDeckId(any())).thenReturn(List.of());

        Job job1 = Job.builder().title("백엔드 엔지니어").build();
        Job job2 = Job.builder().title("데이터 엔지니어").build();
        when(jobRepository.findApplicableJobs(eq(JobStatus.ACTIVE), any(), any()))
                .thenReturn(List.of(job1, job2));

        CardResponse response = new CardResponse(1L, 2L, null, "백엔드 엔지니어", "TODO", null,
                List.of(), null, null, List.of(), null, null, 1, null);
        when(cardService.getDeckCards(any(), any())).thenReturn(List.of(response));

        List<CardResponse> result = recommendationService.generateTodayDeck(1L);

        ArgumentCaptor<List<Card>> cardsCaptor = ArgumentCaptor.forClass(List.class);
        verify(cardRepository).saveAll(cardsCaptor.capture());
        assertThat(cardsCaptor.getValue()).hasSize(2);
        assertThat(cardsCaptor.getValue().get(0).getPosition()).isEqualTo(1);
        assertThat(result).containsExactly(response);
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

        verify(jobRepository, never()).findApplicableJobs(any(), any(), any());
        verify(cardRepository, never()).saveAll(any());
    }
}
