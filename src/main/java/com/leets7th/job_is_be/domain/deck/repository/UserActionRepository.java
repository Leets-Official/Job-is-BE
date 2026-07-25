package com.leets7th.job_is_be.domain.deck.repository;

import com.leets7th.job_is_be.domain.deck.entity.UserAction;
import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    boolean existsByUserIdAndJobIdAndActionType(Long userId, Long jobId, ActionType actionType);

    void deleteByUserIdAndJobIdAndActionType(Long userId, Long jobId, ActionType actionType);
}
