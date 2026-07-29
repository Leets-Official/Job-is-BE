package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.entity.Region;
import lombok.Getter;

@Getter
public class RegionResponse {
    private final Long id;
    private final String name;

    public RegionResponse(Region region) {
        this.id = region.getId();
        this.name = region.getName();
    }
}
