package com.eventsaathi.security;

import com.eventsaathi.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public final class UserPrincipal implements UserDetails {
    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(User user) {
        id = user.getId();
        name = user.getName();
        email = user.getEmail();
        password = user.getPassword();
        active = user.isActive();
        authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .toList();
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user);
    }

    public Long getId() { return id; }
    public String getName() { return name; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}
