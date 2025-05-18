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

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public String login(LoginUserDTO loginUserDTO) {

        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(loginUserDTO.getUsername(), loginUserDTO.getPassword());
        Authentication authResponse = authenticationManager.authenticate(authentication);

        if (null != authResponse && authResponse.isAuthenticated()) {

            User currentUser = (User) authResponse.getPrincipal();
            return jwtUtils.generateJwtToken(currentUser);
        }
        throw new BadCredentialsException("Bad credentials");
    }
}
