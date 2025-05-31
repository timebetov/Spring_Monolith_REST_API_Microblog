package com.github.timebetov.microblog.services.impl;

import com.github.timebetov.microblog.dtos.user.UpdateUserDTO;
import com.github.timebetov.microblog.dtos.user.UserDTO;
import com.github.timebetov.microblog.exceptions.AlreadyExistsException;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import com.github.timebetov.microblog.mappers.UserMapper;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.IUserService;
import com.github.timebetov.microblog.validations.OnlyOwnerOrAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserDao userDao;

    @Override
    public List<UserDTO> getAllUsers() {

        List<UserDTO> users = new ArrayList<>();
        userDao.findAll().forEach(user -> users.add(UserMapper.mapToUserDTO(user)));
        return users;
    }

    @Override
    public UserDTO getByUsername(String username) {

        User user = userDao.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User", username));
        return UserMapper.mapToUserDTO(user);
    }

    @Override
    public UserDTO getById(long userId) {

        User user = userDao.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", String.valueOf(userId)));
        return UserMapper.mapToUserDTO(user);
    }

    @Override
    public UserDTO getByEmail(String email) {

        User user = userDao.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", email));
        return UserMapper.mapToUserDTO(user);
    }

    @OnlyOwnerOrAdmin(ownerIdParam = "userId")
    @Override
    public void updateUser(Long userId, UpdateUserDTO userDetails) {

        User updatedUser = userDao.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", String.valueOf(userId)));

        boolean isUsernameNotNull = userDetails.getUsername() != null;
        if (isUsernameNotNull) {
            boolean isUsernameNotSame = !userDetails.getUsername().equals(updatedUser.getUsername());
            if (isUsernameNotSame) {
                boolean isUsernameAlreadyTaken = userDao.existsByUsername(userDetails.getUsername());
                if (isUsernameAlreadyTaken)
                    throw new AlreadyExistsException("User", userDetails.getUsername());
            }
        }

        boolean isEmailNotNull = userDetails.getEmail() != null;
        if (isEmailNotNull) {
            boolean isEmailNotSame = !userDetails.getEmail().equals(updatedUser.getEmail());
            if (isEmailNotSame) {
                boolean isEmailAlreadyTaken = userDao.existsByEmail(userDetails.getEmail());
                if (isEmailAlreadyTaken)
                    throw new AlreadyExistsException("User", userDetails.getEmail());
            }
        }

        UserMapper.mapUpdateDTOToUser(userDetails, updatedUser);
        userDao.save(updatedUser);
    }

    @OnlyOwnerOrAdmin(ownerIdParam = "userId")
    @Override
    public void deleteUser(Long userId) {

        if (!userDao.existsById(userId))
            throw new ResourceNotFoundException("User", String.valueOf(userId));

        userDao.deleteById(userId);
    }
}
