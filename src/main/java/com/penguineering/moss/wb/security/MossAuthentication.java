package com.penguineering.moss.wb.security;

import com.penguineering.moss.wb.security.directory.MossUserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class MossAuthentication implements Authentication {
    private final MossUserDTO user;

    public MossAuthentication(MossUserDTO user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Objects.nonNull(user.id())
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                : Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return user;
    }

    @Override
    public Object getPrincipal() {
        return user.id();
    }

    @Override
    public boolean isAuthenticated() {
        return Objects.nonNull(user.id());
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (!isAuthenticated) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getName() {
        return user.displayName();
    }
}
