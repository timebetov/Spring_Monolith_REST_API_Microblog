package com.github.timebetov.microblog.models;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@RequiredArgsConstructor
@Builder
public class UserDetailsImpl implements UserDetails {

    private final Long userId;
    private final String username;
    private final String email;
    private final String password;
    private final String role;
    private final String bio;
    private final String picture;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role));
    }

    public static UserDetailsImpl of(User user) {
        return new UserDetailsImpl(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name(),
                user.getBio(),
                user.getPicture()
        );
    }
}
