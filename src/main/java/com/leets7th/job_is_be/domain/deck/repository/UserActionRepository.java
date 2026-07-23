package com.leets7th.job_is_be.domain.deck.repository;

import com.leets7th.job_is_be.domain.deck.entity.UserAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {
}
