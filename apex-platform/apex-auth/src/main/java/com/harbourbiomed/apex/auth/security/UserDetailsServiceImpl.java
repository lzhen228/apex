package com.harbourbiomed.apex.auth.security;

import com.harbourbiomed.apex.common.entity.SysUser;
import com.harbourbiomed.apex.common.mapper.SysUserMapper;
import com.harbourbiomed.apex.common.security.ApexUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new org.springframework.security.authentication.DisabledException("用户已被禁用");
        }
        String authority = "ROLE_" + (user.getRole() != null ? user.getRole().toUpperCase() : "USER");
        return new ApexUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(authority)));
    }

    public SysUser loadSysUser(String username) {
        return sysUserMapper.selectByUsername(username);
    }
}
