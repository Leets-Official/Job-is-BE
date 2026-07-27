package com.leets7th.job_is_be.domain.deck.repository;

import com.leets7th.job_is_be.domain.deck.entity.UserAction;
import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    boolean existsByUserIdAndJobIdAndActionType(Long userId, Long jobId, ActionType actionType);

    void deleteByUserIdAndJobIdAndActionType(Long userId, Long jobId, ActionType actionType);

    @Query("""
            SELECT ua.job.id FROM UserAction ua
            WHERE ua.user.id = :userId
            AND ua.job.id IN :jobIds
            AND ua.actionType = :actionType
            """)
    List<Long> findJobIdsByUserIdAndJobIdInAndActionType(
            @Param("userId") Long userId,
            @Param("jobIds") Collection<Long> jobIds,
            @Param("actionType") ActionType actionType);
}
