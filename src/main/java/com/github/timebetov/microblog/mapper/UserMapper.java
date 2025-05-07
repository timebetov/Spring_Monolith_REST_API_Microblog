package com.github.timebetov.microblog.mapper;

import com.github.timebetov.microblog.dto.user.CreateUserDTO;
import com.github.timebetov.microblog.dto.user.UpdateUserDTO;
import com.github.timebetov.microblog.dto.user.UserDTO;
import com.github.timebetov.microblog.model.User;

public class UserMapper {

    public static UserDTO mapToUserDTO(User user) {

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getUserId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setBio(user.getBio());
        userDTO.setPicture(user.getPicture());
        userDTO.setRole(user.getRole().name());
        userDTO.setCreatedBy(user.getCreatedBy());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedBy(user.getUpdatedBy());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        if (user.getFollowers() != null) {
            userDTO.setFollowers(user.getFollowers().size());
        }
        if (user.getFollows() != null) {
            userDTO.setFollowing(user.getFollows().size());
        }
        if (user.getMoments() != null) {
            userDTO.setMoments(user.getMoments().size());
        }
        return userDTO;
    }

    public static User mapCreateDTOToUser(CreateUserDTO userDTO, User user) {

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        return user;
    }

    public static User mapUpdateDTOToUser(UpdateUserDTO userDTO, User user) {

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
