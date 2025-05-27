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

    private final String usersURI = "/api/users/";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper objectMapper;

    UserDetailsImpl user1;
    UserDetailsImpl user2;
    UserDetailsImpl admin;

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(MockMvcRequestBuilders.get("/").contextPath("/api"))
                .build();

        jdbcTemplate.execute("INSERT INTO users (user_id, username, email, password, created_at, created_by, role) " +
                "VALUES (10, 'user1', 'user1@test.com', 'passwordUser1', CURRENT_TIMESTAMP, 'SYSTEM', 'USER')");
        jdbcTemplate.execute("INSERT INTO users (user_id, username, email, password, created_at, created_by, role) " +
                "VALUES (20, 'user2', 'user2@test.com', 'passwordUser2', CURRENT_TIMESTAMP, 'SYSTEM', 'USER')");
        jdbcTemplate.execute("INSERT INTO users (user_id, username, email, password, created_at, created_by, role) " +
                "VALUES (30, 'admin', 'admin@test.com', 'passwordAdmin', CURRENT_TIMESTAMP, 'SYSTEM', 'ADMIN')");

        user1 = UserDetailsImpl.of(userDao.findById(10L).get());
        user2 = UserDetailsImpl.of(userDao.findById(20L).get());
        admin = UserDetailsImpl.of(userDao.findById(30L).get());
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
    void shouldSaveUserWithValidData() throws Exception {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("benjamin")
                .email("benjamin@test.com")
                .password("passwordTesting2025")
                .confirmPassword("passwordTesting2025")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post(usersURI + "create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("User registered successfully")));

        Optional<User> verifyUser = userDao.findByUsername("benjamin");
        assertTrue(verifyUser.isPresent(), "User not found");
    }

    @Test
    @DisplayName("should return HttpStatus.Bad_Request when saving user with blank data")
    void shouldReturnBadRequestWhenSavingUserWithBlankData() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(usersURI + "create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateUserDTO.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorDetails.username", is("Username cannot be blank")))
                .andExpect(jsonPath("$.errorDetails.email", is("Email Address cannot be blank")))
                .andExpect(jsonPath("$.errorDetails.password", is("Password must be not blank")))
                .andExpect(jsonPath("$.errorDetails.confirmPassword", is("Confirm Password must be not blank")));
    }

    @Test
    @DisplayName("should return HttpStatus.BAD_REQUEST when saving user with non-valid data")
    void shouldReturnBadRequestWhenSavingUserWithNonValidData() throws Exception {

        CreateUserDTO invalidEmailDTO = CreateUserDTO.builder()
                .username("usr")
                .email("invalidEmailAtTestDotcom")
                .password("short")
                .confirmPassword("shorter")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post(usersURI + "create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorDetails.username", is("Username should be at least 4 characters long")))
                .andExpect(jsonPath("$.errorDetails.email", is("Email is not a valid")))
                .andExpect(jsonPath("$.errorDetails.password", is("Password must be between 8 and 20 characters long")))
                .andExpect(jsonPath("$.errorDetails.confirmPassword", is("Passwords do not match!")));
    }

    @Test
    @DisplayName("should return user information of authenticated user")
    void shouldReturnAuthenticatedUserProfile() throws Exception {

        setAuth(user1);

        mockMvc.perform(MockMvcRequestBuilders.get(usersURI + "profile"))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.username", is(user1.getUsername())))
                .andExpect(jsonPath("$.email", is(user1.getEmail())))
                .andExpect(jsonPath("$.role", is(user1.getRole())));
    }

    @Test
    @DisplayName("should return 3 users from db")
    void shouldReturnAllUsers() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(usersURI + "fetch"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("should return user with given ID")
    void shouldReturnUserWhenRetrievingById() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(usersURI + "fetch/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("user1")))
                .andExpect(jsonPath("$.email", is("user1@test.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND when fetching User with id")
    void shouldReturnNotFoundWhenRetrievingUserById() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(usersURI + "fetch/1040"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("should return User with given username")
    void shouldReturnUserWhenRetrievingByUsername() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(usersURI + "fetch/@user1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("user1")))
                .andExpect(jsonPath("$.email", is("user1@test.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND when retrieving user by username")
    void shouldReturnNotFoundWhenRetrievingUserByUsername() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(usersURI + "fetch/@notexisting"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("should return User with given email")
    void shouldReturnUserWhenRetrievingByEmail() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(usersURI + "fetch/email/user1@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email", is("user1@test.com")))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND when retrieving user by email")
    void shouldReturnNotFoundWhenRetrievingUserByEmail() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(usersURI + "fetch/email/notexisting@mail.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("should return HttpStatus.OK when updating user as ADMIN")
    void shouldUpdateAndSaveUserByIdAsAdmin() throws Exception {

        setAuth(admin);

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("updatedUsername")
                .bio("updatedBio")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put(usersURI + "20")
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
    @DisplayName("should return HttpStatus.OK when updating user as owner")
    void shouldUpdateAndSaveUserByIdAsAuthor() throws Exception {

        setAuth(user2);

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("updatedUsername")
                .bio("updatedBio")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put(usersURI + "20")
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

        setAuth(user1);

        UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
                .username("updatedUsername")
                .bio("updatedBio")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put(usersURI + "20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorMessage", is("You don't have permission to access this resource")));

        Optional<User> updatedUser = userDao.findById(20L);
        assertTrue(updatedUser.isPresent(), "User not found");
        assertEquals("user2", updatedUser.get().getUsername(), "username was updated");
        assertNull(updatedUser.get().getBio(), "bio was updated");
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND when updating user")
    void shouldReturnNotFoundWhenUpdatingUser() throws Exception {

        setAuth(admin);

        mockMvc.perform(MockMvcRequestBuilders.put(usersURI + "1040")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateUserDTO.builder().username("newusrname").build())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("should return HttpStatus.Bad_Request when updating user with non-valid data")
    void shouldReturnBadRequestWhenUpdatingUserNonValidData() throws Exception {

        setAuth(admin);

        UpdateUserDTO reqDto = UpdateUserDTO.builder()
                .username("usr")
                .email("nonValidEmail")
                .password("newPWD")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put(usersURI + "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDetails.username", is("Username should be at least 4 characters long")))
                .andExpect(jsonPath("$.errorDetails.email", is("Email is not a valid")))
                .andExpect(jsonPath("$.errorDetails.password",
                        is("Password must be between 8 and 20 characters long")));

    }

    @Test
    @DisplayName("should return HttpStatus.Bad_Request when updating user with blank data")
    void shouldReturnBadRequestWhenUpdatingUserWithBlankData() throws Exception {

        setAuth(admin);
        mockMvc.perform(MockMvcRequestBuilders.put(usersURI + "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateUserDTO.builder().build())))
                .andExpect(jsonPath("$.errorDetails.updateUserDTO", is("At least one field must be provided")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return HttpStatus.No_Content when deleting user")
    void shouldReturnNoContentWhenDeletingUser() throws Exception {

        setAuth(admin);

        mockMvc.perform(MockMvcRequestBuilders.delete(usersURI + "20"))
                .andExpect(status().isNoContent());

        Optional<User> user = userDao.findById(20L);
        assertFalse(user.isPresent(), "User was not deleted");
    }

    @Test
    @DisplayName("should return HttpStatus.FORBIDDEN when deleting other user")
    void shouldReturnForbiddenWhenDeletingUser() throws Exception {

        setAuth(user1);

        mockMvc.perform(MockMvcRequestBuilders.delete(usersURI + "20"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Optional<User> user = userDao.findById(20L);
        assertTrue(user.isPresent(), "User was deleted");
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND when deleting user by id")
    void shouldReturnNotFoundWhenDeletingUser() throws Exception {

        setAuth(admin);

        mockMvc.perform(MockMvcRequestBuilders.delete(usersURI + "1040"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        assertFalse(userDao.existsById(1040L));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        jdbcTemplate.execute("DELETE FROM user_follows");
        jdbcTemplate.execute("DELETE FROM users");
    }
}
