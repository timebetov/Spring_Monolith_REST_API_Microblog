package com.github.timebetov.microblog.service;

import com.github.timebetov.microblog.dto.user.CreateUserDTO;
import com.github.timebetov.microblog.dto.user.UpdateUserDTO;
import com.github.timebetov.microblog.dto.user.UserDTO;

import java.util.List;

public interface IUserService {

    UserDTO createUser(CreateUserDTO userDetails);
    List<UserDTO> getAllUsers();
    UserDTO getByUsername(String username);
    UserDTO getById(long userId);
    UserDTO getByEmail(String email);
    UserDTO updateUser(Long userId, UpdateUserDTO userDetails);
    boolean deleteUser(Long userId);

    boolean followUser(Long followerId, Long followedId);
    boolean unfollowUser(Long followerId, Long followedId);
    List<UserDTO> getFollowers(Long userId);
    List<UserDTO> getFollowings(Long userId);
    boolean isFollowing(Long followerId, Long followedId);

}
