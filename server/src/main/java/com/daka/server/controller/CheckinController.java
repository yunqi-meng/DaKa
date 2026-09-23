package com.daka.server.controller;

import com.daka.server.dto.ApiResponse;
import com.daka.server.dto.CheckinDto;
import com.daka.server.entity.User;
import com.daka.server.service.CheckinService;
import com.daka.server.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    private final CheckinService checkinService;
    private final UserService userService;

    public CheckinController(CheckinService checkinService, UserService userService) {
        this.checkinService = checkinService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public ApiResponse<List<CheckinDto>> list(@RequestHeader("Authorization") String token) {
        User user = userService.getUserByToken(token);
        return ApiResponse.success(checkinService.listByUser(user.getUserId()));
    }

    @PostMapping("/save")
    public ApiResponse<CheckinDto> save(@RequestHeader("Authorization") String token,
                                         @RequestBody CheckinDto dto) {
        User user = userService.getUserByToken(token);
        return ApiResponse.success(checkinService.save(user.getUserId(), dto));
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Void> delete(@RequestHeader("Authorization") String token,
                                     @PathVariable String id) {
        User user = userService.getUserByToken(token);
        checkinService.delete(user.getUserId(), id);
        return ApiResponse.success();
    }
}