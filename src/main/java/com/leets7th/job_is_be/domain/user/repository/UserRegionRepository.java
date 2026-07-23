package com.leets7th.job_is_be.domain.user.repository;

import com.leets7th.job_is_be.domain.user.entity.UserRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRegionRepository extends JpaRepository<UserRegion, Long> {

    Optional<UserRegion> findByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
