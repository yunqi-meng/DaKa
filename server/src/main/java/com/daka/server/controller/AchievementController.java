package com.daka.server.controller;

import com.daka.server.dto.ApiResponse;
import com.daka.server.dto.AchievementDto;
import com.daka.server.dto.UserProgressDto;
import com.daka.server.entity.User;
import com.daka.server.service.AchievementService;
import com.daka.server.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AchievementController {

    private final AchievementService achievementService;
    private final UserService userService;

    public AchievementController(AchievementService achievementService, UserService userService) {
        this.achievementService = achievementService;
        this.userService = userService;
    }

    @PostMapping("/achievement/list")
    public ApiResponse<List<AchievementDto>> list(@RequestHeader("Authorization") String token) {
        User user = userService.getUserByToken(token);
        return ApiResponse.success(achievementService.listByUser(user.getUserId()));
    }

    @PostMapping("/achievement/update")
    public ApiResponse<Void> update(@RequestHeader("Authorization") String token,
                                     @RequestBody AchievementDto dto) {
        User user = userService.getUserByToken(token);
        achievementService.update(user.getUserId(), dto);
        return ApiResponse.success();
    }

    @GetMapping("/progress")
    public ApiResponse<UserProgressDto> progress(@RequestHeader("Authorization") String token) {
        User user = userService.getUserByToken(token);
        return ApiResponse.success(achievementService.getProgress(user.getUserId()));
    }
}