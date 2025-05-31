package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.user.ChangePwdDTO;
import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.LoginUserDTO;

public interface IAuthService {

    void register(CreateUserDTO requestDto);
    String login(LoginUserDTO loginUserDTO);
    void logout(String authHeader);
    void changePassword(Long userId, ChangePwdDTO req);
}
