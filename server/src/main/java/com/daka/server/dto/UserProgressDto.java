package com.daka.server.dto;

import lombok.Data;

@Data
public class UserProgressDto {
    private String userId;
    private Integer totalCheckinCount;
    private Integer totalRevisitProgress;
    private Integer activatedProvinceCount;
    private Integer activatedCityCount;
}