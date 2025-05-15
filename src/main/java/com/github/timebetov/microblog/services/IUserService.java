package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.UpdateUserDTO;
import com.github.timebetov.microblog.dtos.user.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface IUserService extends UserDetailsService {

    void createUser(CreateUserDTO userDetails);
    List<UserDTO> getAllUsers();
    UserDTO getByUsername(String username);
    UserDTO getById(long userId);
    UserDTO getByEmail(String email);
    void updateUser(Long userId, UpdateUserDTO userDetails);
    boolean deleteUser(Long userId);
}
