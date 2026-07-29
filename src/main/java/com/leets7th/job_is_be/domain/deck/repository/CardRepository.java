package com.leets7th.job_is_be.domain.deck.repository;

import com.leets7th.job_is_be.domain.deck.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT c FROM Card c LEFT JOIN FETCH c.job j LEFT JOIN FETCH j.jobCategory " +
            "LEFT JOIN FETCH j.company LEFT JOIN FETCH j.region " +
            "WHERE c.deck.id = :deckId ORDER BY c.position ASC")
    List<Card> findByDeckId(@Param("deckId") Long deckId);
}
