package com.harbourbiomed.apex.auth.controller;

import com.harbourbiomed.apex.auth.dto.LoginRequest;
import com.harbourbiomed.apex.auth.security.AuthService;
import com.harbourbiomed.apex.auth.vo.LoginResponse;
import com.harbourbiomed.apex.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 Controller
 *
 * @author Harbour BioMed
 */
@Tag(name = "认证管理", description = "用户登录、登出等认证相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含 JWT Token）
     */
    @Operation(summary = "用户登录", description = "用户使用用户名和密码登录，返回 JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return Result.ok("登录成功", response);
    }

    /**
     * 用户登出（可选实现）
     *
     * @return 登出结果
     */
    @Operation(summary = "用户登出", description = "用户登出，清除客户端 Token")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // Token 清理在客户端进行，服务端可选择使用 Redis 黑名单机制
        return Result.ok("登出成功", null);
    }
}
