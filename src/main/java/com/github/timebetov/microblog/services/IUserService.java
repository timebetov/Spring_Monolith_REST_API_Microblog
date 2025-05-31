package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.user.UpdateUserDTO;
import com.github.timebetov.microblog.dtos.user.UserDTO;

import java.util.List;

public interface IUserService {

    List<UserDTO> getAllUsers();
    UserDTO getByUsername(String username);
    UserDTO getById(long userId);
    UserDTO getByEmail(String email);
    void updateUser(Long userId, UpdateUserDTO userDetails);
    void deleteUser(Long userId);
}
