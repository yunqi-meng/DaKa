package com.daka.server.dto;

import lombok.Data;
import java.util.List;

@Data
public class SyncBatchRequest {
    private List<CheckinDto> checkins;
    private List<AchievementDto> achievements;
    private UserProgressDto progress;
}