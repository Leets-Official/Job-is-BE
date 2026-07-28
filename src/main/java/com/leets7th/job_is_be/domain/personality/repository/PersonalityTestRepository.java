package com.leets7th.job_is_be.domain.personality.repository;

import com.leets7th.job_is_be.domain.personality.entity.PersonalityTest;
import com.leets7th.job_is_be.domain.personality.enums.PersonalityTestSource;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PersonalityTestRepository extends JpaRepository<PersonalityTest, Long> {

    Optional<PersonalityTest> findFirstByUserIdAndSourceAndCompletedFalseOrderByStartedAtDesc(
            Long userId,
            PersonalityTestSource source
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select test from PersonalityTest test where test.id = :testId")
    Optional<PersonalityTest> findByIdForUpdate(@Param("testId") Long testId);

    Optional<PersonalityTest> findByUserId(Long userId);
}
