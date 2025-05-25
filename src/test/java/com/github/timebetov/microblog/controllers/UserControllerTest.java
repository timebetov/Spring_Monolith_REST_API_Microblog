package com.github.timebetov.microblog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.UpdateUserDTO;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.models.UserDetailsImpl;
import com.github.timebetov.microblog.repository.UserDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(MockMvcRequestBuilders.get("/").contextPath("/api"))
                .build();

        jdbcTemplate.execute("INSERT INTO users (user_id, username, email, password, created_at, created_by, role) " +
                "VALUES (30, 'admin', 'admin@test.com', 'passwordAdmin', CURRENT_TIMESTAMP, 'SYSTEM', 'ADMIN')");
        jdbcTemplate.execute("INSERT INTO users (user_id, username, email, password, created_at, created_by, role) " +
                "VALUES (20, 'user', 'user@test.com', 'passwordUser', CURRENT_TIMESTAMP, 'SYSTEM', 'USER')");
        jdbcTemplate.execute("INSERT INTO users (user_id, username, email, password, created_at, created_by, role) " +
                "VALUES (40, 'user1', 'user1@test.com', 'passwordUser1', CURRENT_TIMESTAMP, 'SYSTEM', 'USER')");
    }

    private void setAuth(UserDetailsImpl user) {

        Authentication authentication = new TestingAuthenticationToken(user, null, "ROLE_USER");
        authentication.setAuthenticated(true);

        SecurityContext secContext = SecurityContextHolder.createEmptyContext();
        secContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(secContext);
    }

    @Test
    @DisplayName("should save new user then return HttpStatus.CREATED with specified message")
    void shouldSaveUser() throws Exception {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("benjamin")
                .email("benjamin@test.com")
                .password("passwordTesting2025")
                .confirmPassword("passwordTesting2025")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("User registered successfully")));

        Optional<User> verifyUser = userDao.findByUsername("benjamin");
        assertTrue(verifyUser.isPresent(), "User not found");
    }

    @Test
    @DisplayName("should return HttpStatus.BAD_REQUEST - Username already exists")
    void shouldReturnUsernameAlreadyExistsBadRequestWhenSavingUser() throws Exception {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("user")
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
    @DisplayName("should return HttpStatus.BAD_REQUEST - Email already exists")
    void shouldReturnEmailAlreadyExistsBadRequestWhenSavingUser() throws Exception {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("user012345")
                .email("user@test.com")
                .password("passwordTesting2025")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("should return HttpStatus.BAD_REQUEST & Not valid Email")
    void shouldReturnInvalidEmailBadRequestWhenSavingUser() throws Exception {

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
    @DisplayName("should return HttpStatus.BAD_REQUEST when saving a new user due to too short password")
    void shouldReturnPasswordTooShortBadRequestWhenSavingUser() throws Exception {

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
    @DisplayName("should return HttpStatus.BAD_REQUEST when saving a new user due to too long password")
    void shouldReturnPasswordTooLongBadRequestWhenSavingUser() throws Exception {

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
    @DisplayName("should return 2 users from db")
    void shouldReturnAllUsers() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("should return user with given ID")
    void shouldReturnUserById() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("user")))
                .andExpect(jsonPath("$.email", is("user@test.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND when fetching User with id")
    void shouldReturnIdNotFoundWhenRetrievingUserWithId() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/1040"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is(HttpStatus.NOT_FOUND.name())));
    }

    @Test
    @DisplayName("should return User with given username")
    void shouldReturnUserByUsername() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/@user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("user")))
                .andExpect(jsonPath("$.email", is("user@test.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND, Username not found when retrieving user by username")
    void shouldReturnUsernameNotFoundWhenRetrievingUserByUsername() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/@notexisting"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is(HttpStatus.NOT_FOUND.name())));
    }

    @Test
    @DisplayName("should return User with given email")
    void shouldReturnUserByEmail() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/email/user@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email", is("user@test.com")))
                .andExpect(jsonPath("$.id", is(20)))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND, Email not found when fetching by email")
    void shouldReturnEmailNotFoundWhenRetrievingUserByEmail() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/email/notexisting@mail.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is(HttpStatus.NOT_FOUND.name())));
    }

    @Test
    @DisplayName("should return HttpStatus.OK & Update and save user in DB as ADMIN")
    void shouldUpdateAndSaveUserByIdAsAdmin() throws Exception {

        setAuth(UserDetailsImpl.builder().userId(30L).role("ADMIN").build());

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("updatedUsername")
                .bio("updatedBio")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("User updated successfully")));

        Optional<User> updatedUser = userDao.findById(20L);
        assertTrue(updatedUser.isPresent(), "User not found");
        assertEquals("updatedUsername", updatedUser.get().getUsername(), "username was not updated");
        assertEquals("updatedBio", updatedUser.get().getBio(), "bio was not updated");
    }

    @Test
    @DisplayName("should return HttpStatus.OK & Update and save user in DB as AUTHOR")
    void shouldUpdateAndSaveUserByIdAsAuthor() throws Exception {

        setAuth(UserDetailsImpl.builder().userId(20L).role("USER").build());

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("updatedUsername")
                .bio("updatedBio")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("User updated successfully")));

        Optional<User> updatedUser = userDao.findById(20L);
        assertTrue(updatedUser.isPresent(), "User not found");
        assertEquals("updatedUsername", updatedUser.get().getUsername(), "username was not updated");
        assertEquals("updatedBio", updatedUser.get().getBio(), "bio was not updated");
    }

    @Test
    @DisplayName("should return HttpStatus.FORBIDDEN when updating other user")
    void shouldReturnForbiddenWhenUpdatingUser() throws Exception {

        setAuth(UserDetailsImpl.builder().userId(40L).role("USER").build());

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("updatedUsername")
                .bio("updatedBio")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorMessage", is("You don't have permission to access this resource")));

        Optional<User> updatedUser = userDao.findById(20L);
        assertTrue(updatedUser.isPresent(), "User not found");
        assertEquals("user", updatedUser.get().getUsername(), "username was updated");
        assertNull(updatedUser.get().getBio(), "bio was updated");
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND user_id not found when updating user as admin")
    void shouldReturnIdNotFoundWhenUpdatingUserAsAdmin() throws Exception {

        setAuth(UserDetailsImpl.builder().userId(30L).role("ADMIN").build());

        UpdateUserDTO dto = UpdateUserDTO.builder().build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/1040")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("should return HttpStatus.BAD_REQUEST, Username already taken when updating user")
    void shouldReturnUsernameAlreadyTakenBadRequestWhenUpdatingUser() throws Exception {

        setAuth(UserDetailsImpl.builder().userId(30L).role("ADMIN").build());

        UpdateUserDTO dto = UpdateUserDTO.builder().username("admin").build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("should return HttpStatus.BAD_REQUEST, Email already taken when updating user")
    void shouldReturnEmailAlreadyTakenBadRequestWhenUpdatingUser() throws Exception {

        setAuth(UserDetailsImpl.builder().userId(30L).role("ADMIN").build());

        UpdateUserDTO dto = UpdateUserDTO.builder().email("admin@test.com").build();
        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("should return HttpStatus.OK, Affect in DB when deleting user")
    void shouldReturnOkWhenDeletingUser() throws Exception {

        setAuth(UserDetailsImpl.builder().userId(30L).role("ADMIN").build());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Optional<User> user = userDao.findById(20L);
        assertFalse(user.isPresent(), "User was not deleted");
    }

    @Test
    @DisplayName("should return HttpStatus.FORBIDDEN when deleting other user")
    void shouldReturnForbiddenWhenDeletingUser() throws Exception {

        setAuth(UserDetailsImpl.builder().userId(40L).role("USER").build());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/20"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Optional<User> user = userDao.findById(20L);
        assertTrue(user.isPresent(), "User was deleted");
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND, Id not found when deleting user by id")
    void shouldReturnIdNotFoundWhenDeletingUser() throws Exception {

        setAuth(UserDetailsImpl.builder().userId(30L).role("ADMIN").build());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/1040"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        jdbcTemplate.execute("DELETE FROM user_follows");
        jdbcTemplate.execute("DELETE FROM users");
    }
}
