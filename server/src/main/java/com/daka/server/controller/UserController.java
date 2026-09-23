package com.daka.server.controller;

import com.daka.server.dto.ApiResponse;
import com.daka.server.dto.LoginRequest;
import com.daka.server.dto.LoginResponse;
import com.daka.server.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            String token = userService.login(request.getUsername(), request.getPassword());
            return ApiResponse.success(new LoginResponse(token, ""));
        } catch (Exception e) {
            return ApiResponse.error(1, e.getMessage());
        }
    }
}