package com.github.timebetov.microblog.services.impl;

import com.github.timebetov.microblog.configs.AppConstants;
import com.github.timebetov.microblog.dtos.user.LoginUserDTO;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.models.UserDetailsImpl;
import com.github.timebetov.microblog.services.IAuthService;
import com.github.timebetov.microblog.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public String login(LoginUserDTO loginUserDTO) {

        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(loginUserDTO.getUsername(), loginUserDTO.getPassword());
        Authentication authResponse = authenticationManager.authenticate(authentication);

        if (null != authResponse && authResponse.isAuthenticated()) {

            UserDetailsImpl currentUser = (UserDetailsImpl) authResponse.getPrincipal();
            return jwtUtils.generateJwtToken(currentUser);
        }
        throw new BadCredentialsException("Bad credentials");
    }

    @Override
    public void logout(String authHeader) {

        String token = authHeader.substring("Bearer ".length());
        Date expirationTime = jwtUtils.extractExpiration(token);

        long ttl = expirationTime.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            tokenBlacklistService.addToBlacklist(token, ttl);
        }
    }
}
