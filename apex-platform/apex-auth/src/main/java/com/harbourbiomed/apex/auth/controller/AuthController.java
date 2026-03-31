package com.harbourbiomed.apex.auth.controller;

import com.harbourbiomed.apex.auth.dto.LoginRequest;
import com.harbourbiomed.apex.auth.dto.FeishuLoginRequest;
import com.harbourbiomed.apex.auth.security.AuthService;
import com.harbourbiomed.apex.auth.vo.LoginResponse;
import com.harbourbiomed.apex.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req.getUsername(), req.getPassword()));
    }

    @Operation(summary = "飞书登录")
    @PostMapping("/feishu/login")
    public Result<LoginResponse> feishuLogin(@Valid @RequestBody FeishuLoginRequest req) {
        return Result.ok(authService.loginWithFeishuCode(req.getCode()));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok(null);
    }
}
