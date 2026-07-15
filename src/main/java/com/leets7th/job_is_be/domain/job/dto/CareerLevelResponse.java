package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.user.enums.CareerLevel;
import lombok.Getter;

@Getter
public class CareerLevelResponse {
    private final String key;          // "NEWCOMER", "JUNIOR" 등
    private final String description;  // "신입", "주니어" 등
    private final Integer minYears;    // SQL career_min 매핑
    private final Integer maxYears;    // SQL career_max 매핑

    public CareerLevelResponse(CareerLevel level) {
        this.key = level.name();
        this.description = level.getDescription();

        // SQL 스키마 기준에 따른 범위 할당
        switch (level) {
            case ENTRY -> {
                this.minYears = 0;
                this.maxYears = 0;
            }
            case JUNIOR -> {
                this.minYears = 1;
                this.maxYears = 3;
            }
            case EXPERIENCED -> {
                this.minYears = 4;
                this.maxYears = 99;
            }
            default -> {
                this.minYears = null;
                this.maxYears = null;
            }
        }
    }
}
