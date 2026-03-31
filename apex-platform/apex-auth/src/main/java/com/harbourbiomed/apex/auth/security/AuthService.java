package com.harbourbiomed.apex.auth.security;

import com.harbourbiomed.apex.auth.vo.LoginResponse;
import com.harbourbiomed.apex.common.entity.SysUser;
import com.harbourbiomed.apex.common.exception.AuthenticationException;
import com.harbourbiomed.apex.common.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final FeishuAuthClient feishuAuthClient;

    public LoginResponse login(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            log.warn("登录失败 username={} cause={}", username, e.getMessage());
            throw new AuthenticationException("用户名或密码错误", e);
        }

        SysUser user = userDetailsService.loadSysUser(username);
        return buildLoginResponse(user);
    }

    public LoginResponse loginWithFeishuCode(String code) {
        FeishuAuthClient.FeishuUserProfile profile = feishuAuthClient.exchangeCode(code);
        String externalId = profile.unionId() != null ? profile.unionId() : profile.openId();
        if (externalId == null) {
            throw new AuthenticationException("飞书登录失败：未获取到用户标识");
        }

        String username = "feishu_" + externalId;
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            user = new SysUser();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setDisplayName(profile.displayName() != null ? profile.displayName() : "飞书用户");
            user.setEmail(profile.email());
            user.setRole("USER");
            user.setStatus(1);
            sysUserMapper.insert(user);
        } else {
            boolean changed = false;
            if (profile.displayName() != null && !profile.displayName().equals(user.getDisplayName())) {
                user.setDisplayName(profile.displayName());
                changed = true;
            }
            if (profile.email() != null && !profile.email().equals(user.getEmail())) {
                user.setEmail(profile.email());
                changed = true;
            }
            if (changed) {
                sysUserMapper.updateById(user);
            }
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new AuthenticationException("用户已被禁用");
        }

        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(SysUser user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .user(LoginResponse.UserVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .displayName(user.getDisplayName())
                        .build())
                .build();
    }
}
