package com.github.timebetov.microblog.mapper;

import com.github.timebetov.microblog.dto.UserDTO;
import com.github.timebetov.microblog.model.User;

public class UserMapper {

    public static UserDTO mapToUserDTO(User user, UserDTO userDTO) {

        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPassword(user.getPassword());
        userDTO.setBio(user.getBio());
        userDTO.setPicture(user.getPicture());
        userDTO.setRole(user.getRole().name());
        return userDTO;
    }

    public static User mapToUser(UserDTO userDTO, User user) {

        if (userDTO.getUsername() != null) {
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPassword() != null) {
            user.setPassword(userDTO.getPassword());
        }
        if (userDTO.getBio() != null) {
            user.setBio(userDTO.getBio());
        }
        if (userDTO.getPicture() != null) {
            user.setPicture(userDTO.getPicture());
        }
        if (userDTO.getRole() != null) {
            user.setRole(User.Role.valueOf(userDTO.getRole()));
        }
        return user;
    }
}
