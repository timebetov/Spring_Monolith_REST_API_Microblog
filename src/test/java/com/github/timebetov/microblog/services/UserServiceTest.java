package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.user.UpdateUserDTO;
import com.github.timebetov.microblog.dtos.user.UserDTO;
import com.github.timebetov.microblog.exceptions.AlreadyExistsException;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    UserDTO userDTO;
    User user;

    @BeforeEach
    void setup() {
        this.userDTO = UserDTO.builder()
                .id(1L)
                .username("alexkey")
                .email("alexkey@gmail.com")
                .role(User.Role.USER.name())
                .build();

        this.user = User.builder()
                .userId(1L)
                .username("alexkey")
                .email("alex@gmail.com")
                .password("Alexkey2@25pwd")
                .role(User.Role.USER)
                .build();
    }

    @Test
    @DisplayName("should return List containing 1 User")
    void shouldReturnUsersWhenRetrievingAllUsers() {

        when(userDao.findAll()).thenReturn(List.of(user));
        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("should return empty list when there no users in db")
    void shouldReturnEmptyListUsersWhenRetrievingAllUsers() {

        when(userDao.findAll()).thenReturn(List.of());
        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(0, users.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("should return user by given username")
    void shouldReturnUserWhenRetrievingByUsername() {

        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.of(user));

        UserDTO foundUser = userService.getByUsername("alexkey");

        assertNotNull(foundUser);
        assertEquals("alexkey", foundUser.getUsername());
        assertEquals(User.Role.USER.name(), foundUser.getRole());

        verify(userDao, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("should throw an ResourceNotFoundException when retrieving user by username")
    void shouldThrowResourceNotFoundExceptionWhenRetrievingUserByUsername() {

        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getByUsername("nonexistent"));
        verify(userDao, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("should return user by given email")
    void shouldReturnUserWhenRetrievingByEmail() {

        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        UserDTO foundUser = userService.getByEmail("alex@gmail.com");

        assertNotNull(foundUser);
        assertEquals("alexkey", foundUser.getUsername());
        assertEquals("alex@gmail.com", foundUser.getEmail());
        assertEquals(User.Role.USER.name(), foundUser.getRole());

        verify(userDao, times(1)).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("should throw an ResourceNotFoundException when retrieving user by email")
    void shouldThrowResourceNotFoundExceptionWhenRetrievingUserByEmail() {

        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getByEmail("nonexistent@gmail.com"));
        verify(userDao, times(1)).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("should return user by given id")
    void shouldReturnUserWhenRetrievingById() {

        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        UserDTO foundUser = userService.getById(1L);
        assertNotNull(foundUser);
        assertEquals("alexkey", foundUser.getUsername());
        assertEquals("alex@gmail.com", foundUser.getEmail());
        assertEquals(User.Role.USER.name(), foundUser.getRole());

        verify(userDao, times(1)).findById(1L);
    }

    @Test
    @DisplayName("should throw an ResourceNotFoundException when retrieving user by given id")
    void shouldThrowResourceNotFoundExceptionWhenRetrievingUserById() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getById(0L));
        verify(userDao, times(1)).findById(0L);
    }

    @Test
    @DisplayName("should update user if exists and data is valid and unique")
    void shouldUpdateUserIfExistsAndDataIsValidAndUnique() {

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("updatedalexkey")
                .email("updalex@gmail.com")
                .build();

        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.existsByUsername(any(String.class))).thenReturn(false);
        when(userDao.existsByEmail(any(String.class))).thenReturn(false);

        assertDoesNotThrow(() -> userService.updateUser(1L, updateUserDTO));

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).existsByUsername(any(String.class));
        verify(userDao, times(1)).existsByEmail(any(String.class));

        verify(userDao, times(1)).save(any(User.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("updatedalexkey", savedUser.getUsername());
        assertEquals("updalex@gmail.com", savedUser.getEmail());
    }

    @Test
    @DisplayName("should throw an ResourceNotFoundException when updating user by given id")
    void shouldThrowResourceNotFoundExceptionWhenUpdatingUserById() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(0L, new UpdateUserDTO()));
        verify(userDao, times(1)).findById(0L);
    }

    @Test
    @DisplayName("should throw an AlreadyExistsException when updating due to username has already taken")
    void shouldThrowUsernameAlreadyExistsExceptionWhenUpdatingUser() {

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("testusername")
                .email("testemail@gmail.com")
                .build();

        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        when(userDao.existsByUsername(any(String.class))).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(1L, updateUserDTO));

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).existsByUsername(any(String.class));
    }

    @Test
    @DisplayName("should throw an AlreadyExistsException when updating due to Email has already taken")
    void shouldThrowEmailAlreadyExistsExceptionWhenUpdatingUser() {

        User toUpdate = User.builder()
                .username("testusername")
                .email("testemail@gmail.com")
                .role(User.Role.USER)
                .build();

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("alexkey")
                .email("alex@gmail.com")
                .build();

        when(userDao.findById(1L)).thenReturn(Optional.of(toUpdate));
        when(userDao.existsByUsername(any(String.class))).thenReturn(false);
        when(userDao.existsByEmail(any(String.class))).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(1L, updateUserDTO));

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).existsByUsername(any(String.class));
        verify(userDao, times(1)).existsByEmail(any(String.class));
    }

    @Test
    @DisplayName("should delete user by given id")
    void shouldDeleteUserById() {

        when(userDao.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userDao, times(1)).existsById(1L);
        verify(userDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("should throw an ResourceNotFoundException when deleting not existing user")
    void shouldThrowResourceNotFoundExceptionWhenDeletingUserById() {

        when(userDao.existsById(0L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(0L));
        verify(userDao, times(1)).existsById(0L);
    }
}
