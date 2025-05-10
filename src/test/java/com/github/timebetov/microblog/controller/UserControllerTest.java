package com.github.timebetov.microblog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.timebetov.microblog.dto.user.CreateUserDTO;
import com.github.timebetov.microblog.dto.user.UpdateUserDTO;
import com.github.timebetov.microblog.model.User;
import com.github.timebetov.microblog.repository.UserDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerTest {

    private static MockHttpServletRequest request;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserController userController;

    @Autowired
    private UserDao userDao;

    private User user;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        request = new MockHttpServletRequest();
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testing");
        user.setEmail("testing@test.com");
        user.setPassword("passwordTesting");
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy("SYSTEM");
        user.setRole(User.Role.ADMIN);

        jdbcTemplate.execute("INSERT INTO users (user_id, username, email, password, created_at, created_by, role) " +
                "VALUES (2, 'test1', 'test1@test.com', 'passwordTest1', CURRENT_TIMESTAMP, 'SYSTEM', 'USER')");
    }

    @Test
    @DisplayName("createUser: Should return HttpStatus.CREATED")
    void createUser() throws Exception {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("benjamin")
                .email("benjamin@test.com")
                .password("passwordTestingBenjamin2025")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("User registered successfully")));

        Optional<User> verifyUser = userDao.findByUsername("benjamin");
        assertNotNull(verifyUser, "User should not be null");
    }

    @Test
    @DisplayName("createUser: Should return HttpStatus.BAD_REQUEST - Username already exists")
    void createUserUsernameAlreadyExists() throws Exception {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("test1")
                .email("someemail@test.com")
                .password("passwordTesting2025")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("createUser: Should return HttpStatus.BAD_REQUEST - Email already exists")
    void createUserEmailAlreadyExists() throws Exception {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("user012345")
                .email("test1@test.com")
                .password("passwordTesting2025")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("createUser: Should return HttpStatus.BAD_REQUEST & Not valid Email")
    void createUserInvalidEmail() throws Exception {

        CreateUserDTO invalidEmailDTO = CreateUserDTO.builder()
                .username("validUsername")
                .email("invalidEmailAtTestDotcom")
                .password("passwordTesting")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorDetails.email", is("Email is not a valid")));
    }

    @Test
    @DisplayName("createUser: Should return HttpStatus.BAD_REQUEST & Password length less than 8")
    void createUserPasswordTooShort() throws Exception {

        CreateUserDTO emptyPasswordDTO = CreateUserDTO.builder()
                .username("validUsername")
                .email("validEmail@test.com")
                .password("")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyPasswordDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is(HttpStatus.BAD_REQUEST.name())))
                .andExpect(jsonPath("$.errorDetails.password", is("Password must be between 8 and 20 characters long")));
    }

    @Test
    @DisplayName("createUser: Should return HttpStatus.BAD_REQUEST & Password length more than 20")
    void createUserPasswordTooLong() throws Exception {

        CreateUserDTO longPasswordDTO = CreateUserDTO.builder()
                .username("validUsername")
                .email("validEmail@test.com")
                .password("qweasdzxc123rtyfghvbn094586")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longPasswordDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is(HttpStatus.BAD_REQUEST.name())))
                .andExpect(jsonPath("$.errorDetails.password", is("Password must be between 8 and 20 characters long")));
    }

    @Test
    @DisplayName("getAllUsers: Should return 2 users")
    void getAllUsersTest() throws Exception {

        entityManager.persist(user);
        entityManager.flush();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("getUserById: Should return user with given ID")
    void getUserByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("test1")))
                .andExpect(jsonPath("$.email", is("test1@test.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("getUserById: Should return HttpStatus.NOT_FOUND")
    void getUserByIdNotFoundTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/1040"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is(HttpStatus.NOT_FOUND.name())));
    }

    @Test
    @DisplayName("getUserByUsername: Should return User with given username")
    void getUserByUsernameTest() throws Exception {

        entityManager.persist(user);
        entityManager.flush();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/@testing"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("testing")))
                .andExpect(jsonPath("$.email", is("testing@test.com")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @DisplayName("getUserByUsername: Should return HttpStatus.NOT_FOUND & Username not found")
    void getUserByUsernameNotFoundTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/@notexisting"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is(HttpStatus.NOT_FOUND.name())));
    }

    @Test
    @DisplayName("getUserByEmail: Should return User with given email")
    void getUserByEmailTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/email/test1@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email", is("test1@test.com")))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("getUserByEmail: Should return HttpStatus.NOT_FOUND & Email not found")
    void getUserByEmailNotFoundTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/email/notexisting@mail.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is(HttpStatus.NOT_FOUND.name())));
    }

    @Test
    @DisplayName("updateUser: Should return HttpStatus.OK & Update user in DB")
    void updateUserTest() throws Exception {

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("updatedUsername")
                .bio("updatedBio")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("User updated successfully")));

        Optional<User> updatedUser = userDao.findById(2L);
        assertNotNull(updatedUser, "Updated user is null");
        assertEquals("updatedUsername", updatedUser.get().getUsername(), "username was not updated");
        assertEquals("updatedBio", updatedUser.get().getBio(), "bio was not updated");
    }

    @Test
    @DisplayName("updateUser: Should return HttpStatus.NOT_FOUND Invalid user_id")
    void updateUserInvalidUserIdTest() throws Exception {

        UpdateUserDTO dto = UpdateUserDTO.builder().build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/1040")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("updateUser: Should return HttpStatus.BAD_REQUEST - Username already taken")
    void updateUserUsernameAlreadyTakenTest() throws Exception {

        entityManager.persist(user);
        entityManager.flush();

        UpdateUserDTO dto = UpdateUserDTO.builder().username("testing").build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("updateUser: Should return HttpStatus.BAD_REQUEST - Email already taken")
    void updateUserEmailAlreadyTakenTest() throws Exception {

        entityManager.persist(user);
        entityManager.flush();

        UpdateUserDTO dto = UpdateUserDTO.builder().email("testing@test.com").build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("deleteUser: Should return HttpStatus.OK & Affect in DB")
    void deleteUserTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Optional<User> user = userDao.findById(2L);
        assertFalse(user.isPresent(), "User was not deleted");
    }

    @Test
    @DisplayName("deleteUser: Should return HttpStatus.NOT_FOUND - Id not found")
    void deleteUserNotFoundTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/1040"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM user_follows");
        jdbcTemplate.execute("DELETE FROM users; ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
    }
}
