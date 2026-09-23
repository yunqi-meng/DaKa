package com.daka.server.controller;

import com.daka.server.dto.ApiResponse;
import com.daka.server.dto.SyncBatchRequest;
import com.daka.server.entity.User;
import com.daka.server.service.AchievementService;
import com.daka.server.service.CheckinService;
import com.daka.server.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final CheckinService checkinService;
    private final AchievementService achievementService;
    private final UserService userService;

    public SyncController(CheckinService checkinService, AchievementService achievementService, UserService userService) {
        this.checkinService = checkinService;
        this.achievementService = achievementService;
        this.userService = userService;
    }

    @PostMapping("/batch")
    public ApiResponse<SyncBatchRequest> syncBatch(@RequestHeader("Authorization") String token,
                                                     @RequestBody SyncBatchRequest request) {
        User user = userService.getUserByToken(token);
        if (request.getCheckins() != null) {
            request.getCheckins().forEach(dto -> checkinService.save(user.getUserId(), dto));
        }
        if (request.getAchievements() != null) {
            request.getAchievements().forEach(dto -> achievementService.update(user.getUserId(), dto));
        }
        if (request.getProgress() != null) {
            achievementService.updateProgress(user.getUserId(), request.getProgress());
        }
        return ApiResponse.success(request);
    }
}