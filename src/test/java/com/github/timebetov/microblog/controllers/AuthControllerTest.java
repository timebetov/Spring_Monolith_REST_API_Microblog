package com.github.timebetov.microblog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.LoginUserDTO;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.repository.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    private final String authURI = "/api/auth/";

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
                "VALUES (10, 'user1', 'user1@test.com', '{bcrypt}$2a$12$3CR2S20qdf2d/g70rSF/aOWm7jopOXh644jIU8lGjJvJ2r.CyivuG', CURRENT_TIMESTAMP, 'SYSTEM', 'USER')");
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

        mockMvc.perform(MockMvcRequestBuilders.post(authURI + "register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("User Registered successfully")));

        Optional<User> verifyUser = userDao.findByUsername("benjamin");
        assertTrue(verifyUser.isPresent(), "User not found");
    }

    @Test
    @DisplayName("should return HttpStatus.Bad_Request when saving user with blank data")
    void shouldReturnBadRequestWhenSavingUserWithBlankData() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(authURI + "register")
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

        mockMvc.perform(MockMvcRequestBuilders.post(authURI + "register")
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
    @DisplayName("should return HttpStatus.OK and JWT Token when logging in")
    void shouldReturnStatusOkAndTokenWhenLogginInWithValidData() throws Exception {

        LoginUserDTO request = LoginUserDTO.builder()
                .username("user1")
                .password("passwordUser1")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post(authURI + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return HttpStatus.Bad_Request Invalid Password when logging in")
    void shouldReturnInvalidPasswordWhenLogging() throws Exception {

        LoginUserDTO request = LoginUserDTO.builder()
                .username("user1")
                .password("invalidsqw")
                .build();
        mockMvc.perform(MockMvcRequestBuilders.post(authURI + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.errorMessage", is("Invalid password")))
                .andExpect(status().isBadRequest());

    }
}
