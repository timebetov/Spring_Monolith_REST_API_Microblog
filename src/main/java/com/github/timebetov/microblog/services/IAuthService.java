package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.user.LoginUserDTO;

public interface IAuthService {

    String login(LoginUserDTO loginUserDTO);
}
