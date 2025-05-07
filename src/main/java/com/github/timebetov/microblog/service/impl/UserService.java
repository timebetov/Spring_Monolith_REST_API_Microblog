package com.github.timebetov.microblog.service.impl;

import com.github.timebetov.microblog.dto.user.CreateUserDTO;
import com.github.timebetov.microblog.dto.user.UpdateUserDTO;
import com.github.timebetov.microblog.dto.user.UserDTO;
import com.github.timebetov.microblog.exception.AlreadyExistsException;
import com.github.timebetov.microblog.exception.ResourceNotFoundException;
import com.github.timebetov.microblog.mapper.UserMapper;
import com.github.timebetov.microblog.model.User;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserDao userDao;

    @Override
    public UserDTO createUser(CreateUserDTO userDetails) {

        if (userDao.findByUsername(userDetails.getUsername()).isPresent()) {
            throw new AlreadyExistsException("User", "username", userDetails.getUsername());
        }
        if (userDao.findByEmail(userDetails.getEmail()).isPresent()) {
            throw new AlreadyExistsException("User", "email", userDetails.getEmail() );
        }

        User newUser = UserMapper.mapCreateDTOToUser(userDetails, new User());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setCreatedBy("SYSTEM");
        newUser.setRole(User.Role.USER);
        return UserMapper.mapToUserDTO(userDao.save(newUser));
    }

    @Override
    public List<UserDTO> getAllUsers() {

        List<UserDTO> users = new ArrayList<>();
        userDao.findAll().forEach(user -> users.add(UserMapper.mapToUserDTO(user)));
        return users;
    }

    @Override
    public UserDTO getByUsername(String username) {

        User user = userDao.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User", "username", username)
        );
        return UserMapper.mapToUserDTO(user);
    }

    @Override
    public UserDTO getById(long userId) {

        User user = userDao.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", String.valueOf(userId))
        );
        return UserMapper.mapToUserDTO(user);
    }

    @Override
    public UserDTO getByEmail(String email) {

        User user = userDao.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", email)
        );
        return UserMapper.mapToUserDTO(user);
    }

    @Override
    public UserDTO updateUser(Long userId, UpdateUserDTO userDetails) {

        User updatedUser = userDao.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", String.valueOf(userId))
        );
        if (userDetails.getUsername() != null && !userDetails.getUsername().equals(updatedUser.getUsername())) {
            if (userDao.findByUsername(userDetails.getUsername()).isPresent()) {
                throw new AlreadyExistsException("User", "username", userDetails.getUsername());
            }
        }
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(updatedUser.getEmail())) {
            if (userDao.findByEmail(userDetails.getEmail()).isPresent()) {
                throw new AlreadyExistsException("User", "email", userDetails.getEmail());
            }
        }

        UserMapper.mapUpdateDTOToUser(userDetails, updatedUser);

        updatedUser.setUpdatedAt(LocalDateTime.now());
        updatedUser.setUpdatedBy("SYSTEM");
        return UserMapper.mapToUserDTO(userDao.save(updatedUser));
    }

    @Override
    public boolean deleteUser(Long userId) {

        boolean result = true;
        if (userDao.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User", "id", String.valueOf(userId));
        }
        userDao.deleteById(userId);
        return result;
    }

    @Override
    public boolean followUser(Long followerId, Long followedId) {
        return false;
    }

    @Override
    public boolean unfollowUser(Long followerId, Long followedId) {
        return false;
    }

    @Override
    public List<UserDTO> getFollowers(Long userId) {
        return List.of();
    }

    @Override
    public List<UserDTO> getFollowings(Long userId) {
        return List.of();
    }

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        return false;
    }
}
