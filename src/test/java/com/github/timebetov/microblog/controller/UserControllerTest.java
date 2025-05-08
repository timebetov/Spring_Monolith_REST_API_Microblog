package com.github.timebetov.microblog.controller;

import com.github.timebetov.microblog.model.User;
import com.github.timebetov.microblog.repository.UserDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
    @DisplayName("getUserById: Should return user")
    void getUserByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/fetch/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("test1")))
                .andExpect(jsonPath("$.email", is("test1@test.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM user_follows");
        jdbcTemplate.execute("DELETE FROM users; ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
    }
}
