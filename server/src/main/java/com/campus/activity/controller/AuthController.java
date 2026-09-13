package com.campus.activity.controller;

import com.campus.activity.common.Result;
import com.campus.activity.dto.LoginRequest;
import com.campus.activity.dto.LoginResponse;
import com.campus.activity.dto.RegisterRequest;
import com.campus.activity.dto.RegisterResponse;
import com.campus.activity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 注册登录接口（公开路径，见 WebConfig 白名单） */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }
}
