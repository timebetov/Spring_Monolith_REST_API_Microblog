package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @MockitoBean
    private UserDao userDao;

    @Autowired
    private UserService userService;

    UserDTO userDTO;
    User user;

    @BeforeEach
    public void setup() {
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
    @DisplayName("should save new user when username and email are unique")
    public void shouldCreateUserWhenUsernameAndEmailAreUnique() {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("alexkey")
                .email("alex@gmail.com")
                .password("Alexkey2@25pwd")
                .build();

        when(userDao.findByUsername(createUserDTO.getUsername())).thenReturn(Optional.empty());
        when(userDao.findByEmail(createUserDTO.getEmail())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> userService.createUser(createUserDTO));
        verify(userDao, times(1)).save(any(User.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("alexkey", savedUser.getUsername());
        assertEquals("alex@gmail.com", savedUser.getEmail());
        assertEquals(User.Role.USER, savedUser.getRole());
    }

    @Test
    @DisplayName("should throw an AlreadyExistsException due to email has already taken")
    public void shouldThrowAlreadyExistsExceptionWhenSavingUserEmailAlreadyExists() {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("alexkey")
                .email("alex@gmail.com")
                .password("Alexkey2@25pwd")
                .build();

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(createUserDTO));
        verify(userDao, times(1)).findByEmail(anyString());
    }

    @Test
    @DisplayName("should throw an AlreadyExistsException due to username has already taken")
    public void shouldThrowAlreadyExistsExceptionWhenSavingUserUsernameAlreadyExists() {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("alexkey")
                .email("alex@gmail.com")
                .password("Alexkey2@25pwd")
                .build();

        when(userDao.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(createUserDTO));
        verify(userDao, times(1)).findByUsername(anyString());
    }

    @Test
    @DisplayName("should return List containing 1 User")
    public void shouldReturnUsers() {

        when(userDao.findAll()).thenReturn(List.of(user));
        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("should return empty list when there no user in db")
    public void shouldReturnEmptyList() {

        when(userDao.findAll()).thenReturn(List.of());
        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(0, users.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("should return user by given username")
    public void shouldReturnUserByUsername() {

        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.of(user));

        UserDTO foundUser = userService.getByUsername("alexkey");

        assertNotNull(foundUser);
        assertEquals("alexkey", foundUser.getUsername());
        assertEquals(User.Role.USER.name(), foundUser.getRole());

        verify(userDao, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("should throw an ResourceNotFoundException due to invalid username")
    public void shouldThrowResourceNotFoundExceptionWhenRetrievingUserByInvalidUsername() {

        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getByUsername("nonexistent"));
        verify(userDao, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("should return user by given email")
    public void shouldReturnUserByEmail() {

        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        UserDTO foundUser = userService.getByEmail("alex@gmail.com");

        assertNotNull(foundUser);
        assertEquals("alexkey", foundUser.getUsername());
        assertEquals("alex@gmail.com", foundUser.getEmail());
        assertEquals(User.Role.USER.name(), foundUser.getRole());

        verify(userDao, times(1)).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("should throw an ResourceNotFoundException")
    public void shouldThrowResourceNotFoundExceptionWhenRetrievingUserByInvalidEmail() {

        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getByEmail("nonexistent@gmail.com"));
        verify(userDao, times(1)).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("should return user by given id")
    public void shouldReturnUserById() {

        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        UserDTO foundUser = userService.getById(1L);
        assertNotNull(foundUser);
        assertEquals("alexkey", foundUser.getUsername());
        assertEquals("alex@gmail.com", foundUser.getEmail());
        assertEquals(User.Role.USER.name(), foundUser.getRole());

        verify(userDao, times(1)).findById(1L);
    }

    @Test
    @DisplayName("should throw an ResourceNotFoundException due to invalid id")
    public void shouldThrowResourceNotFoundExceptionWhenRetrievingUserByInvalidId() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getById(0L));
        verify(userDao, times(1)).findById(0L);
    }

    @Test
    @DisplayName("should update user if exists and data is valid and unique")
    public void shouldUpdateUserIfExistsAndDataIsValidAndUnique() {

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("updatedalexkey")
                .email("updalex@gmail.com")
                .password("newpassword")
                .role("ADMIN")
                .build();

        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.empty());
        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> userService.updateUser(1L, updateUserDTO));

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).findByUsername(any(String.class));
        verify(userDao, times(1)).findByEmail(any(String.class));

        verify(userDao, times(1)).save(any(User.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("updatedalexkey", savedUser.getUsername());
        assertEquals("updalex@gmail.com", savedUser.getEmail());
        assertEquals("newpassword", savedUser.getPassword());
        assertEquals(User.Role.ADMIN, savedUser.getRole());
    }

    @Test
    @DisplayName("should throw an ResourceNotFoundException when updating by invalid id")
    public void shouldThrowResourceNotFoundExceptionWhenUpdatingUserByInvalidId() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(0L, new UpdateUserDTO()));
        verify(userDao, times(1)).findById(0L);
    }

    @Test
    @DisplayName("should throw an AlreadyExistsException due to username has already taken")
    public void shouldThrowAlreadyExistsExceptionWhenUpdatingUserUsernameAlreadyTaken() {

        User toUpdate = User.builder()
                .username("testusername")
                .email("testemail@gmail.com")
                .role(User.Role.USER)
                .build();

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("alexkey")
                .email("updalex@gmail.com")
                .password("newpassword")
                .role("ADMIN")
                .build();

        when(userDao.findById(1L)).thenReturn(Optional.of(toUpdate));

        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.of(user));

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(1L, updateUserDTO));

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("should throw an AlreadyExistsException due to Email has already taken")
    public void shouldThrowAlreadyExistsExceptionWhenUpdatingUserEmailAlreadyTaken() {

        User toUpdate = User.builder()
                .username("testusername")
                .email("testemail@gmail.com")
                .role(User.Role.USER)
                .build();

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("alexkey")
                .email("alex@gmail.com")
                .password("newpassword")
                .role("ADMIN")
                .build();

        when(userDao.findById(1L)).thenReturn(Optional.of(toUpdate));
        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.empty());
        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(1L, updateUserDTO));

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).findByUsername(any(String.class));
        verify(userDao, times(1)).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("should delete user by id")
    public void shouldDeleteUserById() {

        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        boolean isDeleted = userService.deleteUser(1L);

        assertTrue(isDeleted);
        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("should throw an ResourceNotFoundException when deleting not existing user")
    public void shouldThrowResourceNotFoundExceptionWhenDeletingUserByInvalidId() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(0L));
        verify(userDao, times(1)).findById(0L);
    }
}
