package com.harbourbiomed.apex.auth.security;

import com.harbourbiomed.apex.auth.vo.LoginResponse;
import com.harbourbiomed.apex.common.entity.SysUser;
import com.harbourbiomed.apex.common.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 *
 * @author Harbour BioMed
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录响应
     */
    public LoginResponse login(String username, String password) {
        try {
            // 认证用户（会调用 UserDetailsServiceImpl 验证密码）
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 加载用户信息
            SysUser user = userDetailsService.loadSysUser(username);

            if (user == null) {
                throw new AuthenticationException("用户名或密码错误");
            }

            // 生成 Token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());

            // 构建登录响应
            return LoginResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .displayName(user.getDisplayName())
                    .role(user.getRole())
                    .build();

        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }
            throw new AuthenticationException("用户名或密码错误", e);
        }
    }

    /**
     * 登出（可选实现）
     *
     * @param token JWT Token
     */
    public void logout(String token) {
        // 可选：将 Token 加入 Redis 黑名单
        // TODO: 实现 Token 黑名单机制
    }
}
