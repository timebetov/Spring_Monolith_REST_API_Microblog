package com.github.timebetov.microblog.service;

import com.github.timebetov.microblog.dto.user.CreateUserDTO;
import com.github.timebetov.microblog.dto.user.UpdateUserDTO;
import com.github.timebetov.microblog.dto.user.UserDTO;
import com.github.timebetov.microblog.exception.AlreadyExistsException;
import com.github.timebetov.microblog.exception.ResourceNotFoundException;
import com.github.timebetov.microblog.model.User;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.service.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
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
    @DisplayName("createUser: Should create a brand new User")
    public void createUserTest() {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("alexkey")
                .email("alex@gmail.com")
                .password("Alexkey2@25pwd")
                .build();

        when(userDao.save(any(User.class))).thenReturn(user);

        UserDTO createdUser = userService.createUser(createUserDTO);

        assertNotNull(createdUser, "UserDTO should not be null");
        assertEquals("alexkey", createdUser.getUsername());
        assertEquals("alex@gmail.com", createdUser.getEmail());
        assertEquals(User.Role.USER.name(), createdUser.getRole());
        assertNull(createdUser.getBio());
        assertNull(createdUser.getPicture());
        assertEquals(0, createdUser.getMoments());

        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createUserInvalidEmail: Should throw an AlreadyExistsException")
    public void createUserEmailExistsExceptionTest() {

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
    @DisplayName("createUserInvalidUsername: Should throw an AlreadyExistsException")
    public void createUserUsernameExistsExceptionTest() {

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
    @DisplayName("getAllUsers: Should return List containing 1 User")
    public void getAllUsersTest() {

        when(userDao.findAll()).thenReturn(List.of(user));
        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllUsers: Should return empty list")
    public void getAllUsersEmptyTest() {

        when(userDao.findAll()).thenReturn(List.of());
        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(0, users.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("getUserByUsername: Should return user by given username")
    public void getUserByUsernameTest() {

        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.of(user));

        UserDTO foundUser = userService.getByUsername("alexkey");

        assertNotNull(foundUser);
        assertEquals("alexkey", foundUser.getUsername());
        assertEquals(User.Role.USER.name(), foundUser.getRole());

        verify(userDao, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("getUserInvalidUsername: Should throw an ResourceNotFoundException")
    public void getUserByNonValidUsernameExceptionTest() {

        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getByUsername("nonexistent"));
        verify(userDao, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("getUserByEmail: Should return user by given email")
    public void getUserByEmailTest() {

        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        UserDTO foundUser = userService.getByEmail("alex@gmail.com");

        assertNotNull(foundUser);
        assertEquals("alexkey", foundUser.getUsername());
        assertEquals("alex@gmail.com", foundUser.getEmail());
        assertEquals(User.Role.USER.name(), foundUser.getRole());

        verify(userDao, times(1)).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("getUserInvalidEmail: Should throw an ResourceNotFoundException")
    public void getUserByNonValidEmailExceptionTest() {

        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getByEmail("nonexistent@gmail.com"));
        verify(userDao, times(1)).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("getUser: Should return user by given id")
    public void getUserByIdTest() {

        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        UserDTO foundUser = userService.getById(1L);
        assertNotNull(foundUser);
        assertEquals("alexkey", foundUser.getUsername());
        assertEquals("alex@gmail.com", foundUser.getEmail());
        assertEquals(User.Role.USER.name(), foundUser.getRole());

        verify(userDao, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getUserInvalidId: Should throw an ResourceNotFoundException")
    public void getUserByIdInvalidUserIdExceptionTest() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getById(0L));
        verify(userDao, times(1)).findById(0L);
    }

    @Test
    @DisplayName("updateUser: Should return updated UserDTO")
    public void updateUserTest() {

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("updatedalexkey")
                .email("updalex@gmail.com")
                .password("newpassword")
                .role("ADMIN")
                .build();

        User updatedUser = User.builder()
                .username("updatedalexkey")
                .email("updalex@gmail.com")
                .password("newpassword")
                .role(User.Role.ADMIN)
                .build();

        updatedUser.setUpdatedAt(LocalDateTime.now());
        updatedUser.setUpdatedBy("SYSTEM");

        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.save(any(User.class))).thenReturn(updatedUser);

        UserDTO updatedUserDTO = userService.updateUser(1L, updateUserDTO);

        assertNotNull(updatedUser);
        assertEquals("updatedalexkey", updatedUserDTO.getUsername());
        assertEquals("updalex@gmail.com", updatedUserDTO.getEmail());
        assertEquals(User.Role.ADMIN.name(), updatedUserDTO.getRole());
        assertNotNull(updatedUserDTO.getUpdatedAt());
        assertEquals("SYSTEM", updatedUserDTO.getUpdatedBy());

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserInvalidId: Should throw an ResourceNotFoundException")
    public void updateUserByIdInvalidUserIdExceptionTest() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(0L, new UpdateUserDTO()));
        verify(userDao, times(1)).findById(0L);
    }

    @Test
    @DisplayName("updateUserUsernameAlreadyExists: Should throw an AlreadyExistsException")
    public void updateUserByIdUsernameAlreadyExistsExceptionTest() {

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
    @DisplayName("updateUserEmailAddressAlreadyExists: Should throw an AlreadyExistsException")
    public void updateUserByIdEmailAlreadyExistsExceptionTest() {

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
        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(1L, updateUserDTO));

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).findByUsername(any(String.class));
        verify(userDao, times(1)).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("deleteUser: Should return true")
    public void deleteUserByIdTest() {

        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        boolean isDeleted = userService.deleteUser(1L);

        assertTrue(isDeleted);
        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUserInvalidId: Should throw an ResourceNotFoundException")
    public void deleteUserInvalidUserIdExceptionTest() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(0L));
        verify(userDao, times(1)).findById(0L);
    }
}
