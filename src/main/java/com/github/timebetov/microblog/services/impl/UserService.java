package com.github.timebetov.microblog.services.impl;

import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.UpdateUserDTO;
import com.github.timebetov.microblog.dtos.user.UserDTO;
import com.github.timebetov.microblog.exceptions.AlreadyExistsException;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import com.github.timebetov.microblog.mappers.UserMapper;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserDao userDao;
    private final PasswordEncoder pwdEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User: " + username + " not found"));
    }

    @Override
    public void createUser(CreateUserDTO userDetails) {

        if (userDao.findByUsername(userDetails.getUsername()).isPresent()) {
            throw new AlreadyExistsException("User", "username", userDetails.getUsername());
        }
        if (userDao.findByEmail(userDetails.getEmail()).isPresent()) {
            throw new AlreadyExistsException("User", "email", userDetails.getEmail() );
        }

        String hashedPwd = pwdEncoder.encode(userDetails.getPassword());

        User newUser = UserMapper.mapCreateDTOToUser(userDetails, new User());
        newUser.setPassword(hashedPwd);
        newUser.setCreatedBy("SYSTEM");
        newUser.setRole(User.Role.USER);
        userDao.save(newUser);
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
    public void updateUser(Long userId, UpdateUserDTO userDetails) {

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
        userDao.save(updatedUser);
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
}
