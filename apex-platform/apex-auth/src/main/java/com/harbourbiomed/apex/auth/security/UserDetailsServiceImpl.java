package com.harbourbiomed.apex.auth.security;

import com.harbourbiomed.apex.common.entity.SysUser;
import com.harbourbiomed.apex.common.exception.AuthenticationException;
import com.harbourbiomed.apex.common.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户详情服务实现
 *
 * @author Harbour BioMed
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户信息
        SysUser user = sysUserMapper.selectByUsername(username);

        if (user == null) {
            throw new AuthenticationException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new AuthenticationException("用户已被禁用");
        }

        // 构建用户权限列表
        String role = user.getRole();
        String authority = "ROLE_" + (role != null ? role.toUpperCase() : "USER");

        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authority)
                .build();
    }

    /**
     * 加载完整用户信息（用于登录响应）
     *
     * @param username 用户名
     * @return 用户信息
     */
    public SysUser loadSysUser(String username) {
        return sysUserMapper.selectByUsername(username);
    }
}
