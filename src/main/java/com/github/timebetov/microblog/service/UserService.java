package com.github.timebetov.microblog.service;

import com.github.timebetov.microblog.dto.UserDTO;
import com.github.timebetov.microblog.exception.AlreadyExistsException;
import com.github.timebetov.microblog.exception.ResourceNotFoundException;
import com.github.timebetov.microblog.mapper.UserMapper;
import com.github.timebetov.microblog.model.User;
import com.github.timebetov.microblog.repository.UserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;

    public UserDTO createUser(UserDTO newUserDTO) {

        if (userDao.findByUsername(newUserDTO.getUsername()).isPresent()) {
            throw new AlreadyExistsException(newUserDTO.getUsername() + " already exists");
        }
        if (userDao.findByEmail(newUserDTO.getEmail()).isPresent()) {
            throw new AlreadyExistsException("Email " + newUserDTO.getEmail() + " already exists");
        }

        User newUser = UserMapper.mapToUser(newUserDTO, new User());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setCreatedBy("SYSTEM");
        newUser.setRole(User.Role.USER);
        return UserMapper.mapToUserDTO(userDao.save(newUser), newUserDTO);
    }

    public List<UserDTO> getAllUsers() {

        List<UserDTO> users = new ArrayList<>();
        userDao.findAll().forEach(user -> users.add(UserMapper.mapToUserDTO(user, new UserDTO())));
        return users;
    }

    public UserDTO getByUsername(String username) {

        User user = userDao.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User", "username", username)
        );
        return UserMapper.mapToUserDTO(user, new UserDTO());
    }

    public UserDTO getById(long userId) {

        User user = userDao.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", String.valueOf(userId))
        );
        return UserMapper.mapToUserDTO(user, new UserDTO());
    }

    public UserDTO getByEmail(String email) {

        User user = userDao.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", email)
        );
        return UserMapper.mapToUserDTO(user, new UserDTO());
    }

    public UserDTO updateUser(Long userId, UserDTO userDetails) {

        User updatedUser = userDao.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", String.valueOf(userId))
        );
        if (userDetails.getUsername() != null && !userDetails.getUsername().equals(updatedUser.getUsername())) {
            if (userDao.findByUsername(userDetails.getUsername()).isPresent()) {
                throw new AlreadyExistsException(userDetails.getUsername() + " already exists");
            }
        }
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(updatedUser.getEmail())) {
            if (userDao.findByEmail(userDetails.getEmail()).isPresent()) {
                throw new AlreadyExistsException(userDetails.getEmail() + " already exists");
            }
        }

        UserMapper.mapToUser(userDetails, updatedUser);

        updatedUser.setUpdatedAt(LocalDateTime.now());
        updatedUser.setUpdatedBy("SYSTEM");
        return UserMapper.mapToUserDTO(userDao.save(updatedUser), new UserDTO());
    }

    public boolean deleteUser(Long userId) {

        boolean result = true;
        if (userDao.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User", "id", String.valueOf(userId));
        }
        userDao.deleteById(userId);
        return result;
    }
}
