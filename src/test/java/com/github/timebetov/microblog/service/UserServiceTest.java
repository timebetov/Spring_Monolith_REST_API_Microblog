package com.github.timebetov.microblog.service;

import com.github.timebetov.microblog.dto.UserDTO;
import com.github.timebetov.microblog.exception.AlreadyExistsException;
import com.github.timebetov.microblog.exception.ResourceNotFoundException;
import com.github.timebetov.microblog.model.User;
import com.github.timebetov.microblog.repository.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        this.userDTO = new UserDTO();
        userDTO.setUsername("alexkey");
        userDTO.setEmail("alex@gmail.com");
        userDTO.setPassword("Alexkey2@25pwd");

        this.user = User.builder()
                .userId(1L)
                .username("alexkey")
                .email("alex@gmail.com")
                .password("Alexkey2@25pwd")
                .role(User.Role.USER)
                .build();
    }

    @Test
    @DisplayName("Should create a new User")
    public void createUserTest() {

        when(userDao.save(any(User.class))).thenReturn(user);

        UserDTO createdUser = userService.createUser(userDTO);

        assertNotNull(createdUser, "UserDTO should not be null");
        assertEquals("alexkey", createdUser.getUsername());
        assertEquals("alex@gmail.com", createdUser.getEmail());
        assertEquals(User.Role.USER.name(), createdUser.getRole());

        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw an Email Already Exists Exception")
    public void createUserEmailExistsExceptionTest() {

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(userDTO));
        verify(userDao, times(1)).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should throw an Username Already Exists Exception")
    public void createUserUsernameExistsExceptionTest() {

        when(userDao.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(userDTO));
        verify(userDao, times(1)).findByUsername(anyString());
    }

    @Test
    @DisplayName("Should return list of all existed users in DB")
    public void getAllUsersTest() {

        when(userDao.findAll()).thenReturn(List.of(user));
        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list")
    public void getAllUsersEmptyTest() {

        when(userDao.findAll()).thenReturn(List.of());
        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(0, users.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return user by username")
    public void getUserByUsernameTest() {

        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.of(user));

        UserDTO foundUser = userService.getByUsername("alexkey");

        assertNotNull(foundUser);
        assertEquals("alexkey", foundUser.getUsername());
        assertEquals(User.Role.USER.name(), foundUser.getRole());

        verify(userDao, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("Should throw an Resource Not Found Exception Invalid Username")
    public void getUserByNonValidUsernameExceptionTest() {

        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getByUsername("nonexistent"));
        verify(userDao, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("Should return user by given email")
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
    @DisplayName("Should throw an Resource Not Found Exception Invalid Email")
    public void getUserByNonValidEmailExceptionTest() {

        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getByEmail("nonexistent@gmail.com"));
        verify(userDao, times(1)).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("Should return user by given id")
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
    @DisplayName("Should throw an Resource Not Found Exception Invalid User Id")
    public void getUserByIdInvalidUserIdExceptionTest() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getById(0L));
        verify(userDao, times(1)).findById(0L);
    }

    @Test
    @DisplayName("Should return updated UserDTO")
    public void updateUserTest() {

        UserDTO toUpdate = new UserDTO();
        toUpdate.setUsername("updatedalexkey");
        toUpdate.setEmail("updalex@gmail.com");
        toUpdate.setRole("ADMIN");

        this.user.setUsername("updatedalexkey");
        this.user.setEmail("updalex@gmail.com");
        this.user.setRole(User.Role.ADMIN);

        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.save(any(User.class))).thenReturn(user);

        UserDTO updatedUser = userService.updateUser(1L, toUpdate);

        assertNotNull(updatedUser);
        assertEquals("updatedalexkey", updatedUser.getUsername());
        assertEquals("updalex@gmail.com", updatedUser.getEmail());
        assertEquals(User.Role.ADMIN.name(), updatedUser.getRole());

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw an Resource Not Found Exception Invalid Id to Update")
    public void updateUserByIdInvalidUserIdExceptionTest() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(0L, new UserDTO()));
        verify(userDao, times(1)).findById(0L);
    }

    @Test
    @DisplayName("Should throw an Already Exists Exception Invalid Username to Update")
    public void updateUserByIdUsernameAlreadyExistsExceptionTest() {

        User toUpdate = User.builder()
                .username("testusername")
                .email("testemail@gmail.com")
                .role(User.Role.USER)
                .build();

        when(userDao.findById(1L)).thenReturn(Optional.of(toUpdate));
        when(userDao.findByUsername(any(String.class))).thenReturn(Optional.of(user));

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(1L, userDTO));

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("Should throw an Already Exists Exception Invalid Email to Update")
    public void updateUserByIdEmailAlreadyExistsExceptionTest() {

        User toUpdate = User.builder()
                .username("testusername")
                .email("testemail@gmail.com")
                .role(User.Role.USER)
                .build();

        when(userDao.findById(1L)).thenReturn(Optional.of(toUpdate));
        when(userDao.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(1L, userDTO));

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).findByUsername(any(String.class));
        verify(userDao, times(1)).findByEmail(any(String.class));
    }

    @Test
    @DisplayName("Should return True after Deleting User By Id")
    public void deleteUserByIdTest() {

        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        boolean isDeleted = userService.deleteUser(1L);

        assertTrue(isDeleted);
        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw an Resource Not Found Exception Invalid Id to Delete")
    public void deleteUserInvalidUserIdExceptionTest() {

        when(userDao.findById(0L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(0L));
        verify(userDao, times(1)).findById(0L);
    }

}
