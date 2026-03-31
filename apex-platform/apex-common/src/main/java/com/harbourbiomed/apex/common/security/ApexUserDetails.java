package com.harbourbiomed.apex.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class ApexUserDetails extends User {

    private final Long userId;

    public ApexUserDetails(Long userId, String username, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }
}
