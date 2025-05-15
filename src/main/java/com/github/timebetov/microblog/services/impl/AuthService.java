package com.github.timebetov.microblog.services.impl;

import com.github.timebetov.microblog.dtos.user.LoginUserDTO;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.services.IAuthService;
import com.github.timebetov.microblog.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public String login(LoginUserDTO loginUserDTO) {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginUserDTO.getUsername(),
                loginUserDTO.getPassword());
        Authentication authResponse = authenticationManager.authenticate(authentication);

        if (null != authResponse && authResponse.isAuthenticated()) {

            User currentUser = (User) authResponse.getPrincipal();
            return currentUser.getUsername();
        }
        throw new BadCredentialsException("Bad credentials");
    }

    private String generateJwtToken(User userDetails) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getUserId());
        claims.put("email", userDetails.getEmail());
        claims.put("role", userDetails.getRole());

        return jwtUtils.generateToken(claims, userDetails.getUsername());
    }
}
