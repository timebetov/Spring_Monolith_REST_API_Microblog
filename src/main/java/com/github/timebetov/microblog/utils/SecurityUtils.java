package com.github.timebetov.microblog.utils;

import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.models.UserDetailsImpl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentUserId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new BadCredentialsException("User is not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userDetails.getUserId();
    }

    public static CurrentUserContext getCurrentUserContext() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new BadCredentialsException("User is not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return CurrentUserContext.builder()
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(User.Role.valueOf(userDetails.getRole()))
                .build();
    }
}
