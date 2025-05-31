package com.github.timebetov.microblog.mappers;

import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.UpdateUserDTO;
import com.github.timebetov.microblog.dtos.user.UserDTO;
import com.github.timebetov.microblog.models.User;

public class UserMapper {

    private UserMapper() {}

    public static UserDTO mapToUserDTO(User user) {

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getUserId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setBio(user.getBio());
        userDTO.setRole(user.getRole().name());
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
        return user;
    }

    public static User mapUpdateDTOToUser(UpdateUserDTO userDTO, User user) {

        if (userDTO.getUsername() != null && !userDTO.getUsername().isBlank()) {
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().isBlank()) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getBio() != null && !userDTO.getBio().isBlank()) {
            user.setBio(userDTO.getBio());
        }
        return user;

    }
}
