package com.github.timebetov.microblog.services.impl;

import com.github.timebetov.microblog.dtos.user.ChangePwdDTO;
import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.LoginUserDTO;
import com.github.timebetov.microblog.exceptions.AlreadyExistsException;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import com.github.timebetov.microblog.mappers.UserMapper;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.models.UserDetailsImpl;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.IAuthService;
import com.github.timebetov.microblog.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserDao userDao;
    private final PasswordEncoder pwdEncoder;

    @Override
    public void register(CreateUserDTO requestDto) {

        if (userDao.existsByUsername(requestDto.getUsername()))
            throw new AlreadyExistsException("User", requestDto.getUsername());

        if (userDao.existsByEmail(requestDto.getEmail()))
            throw new AlreadyExistsException("User", requestDto.getEmail());

        String hashedPwd = pwdEncoder.encode(requestDto.getPassword());

        User newUser = UserMapper.mapCreateDTOToUser(requestDto, new User());
        newUser.setPassword(hashedPwd);
        newUser.setRole(User.Role.USER);
        userDao.save(newUser);
    }

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
        if (ttl > 0)
            tokenBlacklistService.addToBlacklist(token, ttl);
    }

    @Override
    public void changePassword(Long userId, ChangePwdDTO req) {

        User foundUser = userDao.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", String.valueOf(userId)));

        if (!pwdEncoder.matches(req.getCurrentPassword(), foundUser.getPassword()))
            throw new BadCredentialsException("Password not valid");

        String hashPassword = pwdEncoder.encode(req.getNewPassword());
        foundUser.setPassword(hashPassword);

        userDao.save(foundUser);
    }
}
