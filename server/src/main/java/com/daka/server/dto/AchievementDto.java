package com.daka.server.dto;

import lombok.Data;

@Data
public class AchievementDto {
    private String id;
    private String name;
    private String description;
    private String category;
    private Integer target;
    private Boolean isUnlocked;
    private Long unlockTime;
    private Integer progress;
}