package com.github.timebetov.microblog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.timebetov.microblog.models.UserDetailsImpl;
import com.github.timebetov.microblog.repository.UserDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FollowControllerTest {

    private final String followURI = "/api/users/follow/";
    private final String unfollowURI = "/api/users/unfollow/";
    private final String followersURI = "/api/users/followers/";
    private final String followingsURI = "/api/users/followings/";
    private final Long nonExistingUserId = 99L;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDao userDao;

    UserDetailsImpl user1;
    UserDetailsImpl user2;
    UserDetailsImpl user3;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(MockMvcRequestBuilders.get("/").contextPath("/api"))
                .build();

        jdbcTemplate.execute("INSERT INTO users (user_id, email, username, password, role, created_at, created_by) " +
                "VALUES (1, 'user1@test.com', 'user1', 'user1PWD', 'USER', CURRENT_TIMESTAMP, 'SYSTEM')");
        jdbcTemplate.execute("INSERT INTO users (user_id, email, username, password, role, created_at, created_by) " +
                "VALUES (2, 'user2@test.com', 'user2', 'user2PWD', 'USER', CURRENT_TIMESTAMP, 'SYSTEM')");
        jdbcTemplate.execute("INSERT INTO users (user_id, email, username, password, role, created_at, created_by) " +
                "VALUES (3, 'user3@test.com', 'user3', 'user3PWD', 'USER', CURRENT_TIMESTAMP, 'SYSTEM')");

        user1 = UserDetailsImpl.builder().username("user1").userId(1L).role("USER").build();
        user2 = UserDetailsImpl.builder().username("user2").userId(2L).role("USER").build();
        user3 = UserDetailsImpl.builder().username("user3").userId(3L).role("USER").build();

        jdbcTemplate.execute("INSERT INTO user_follows(follower_id, followed_id) VALUES(2, 1)");
    }

    private void setAuth(UserDetailsImpl user) {

        Authentication authentication = new TestingAuthenticationToken(user, null, user.getAuthorities());
        authentication.setAuthenticated(true);

        SecurityContext secContext = SecurityContextHolder.createEmptyContext();
        secContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(secContext);
    }

    @Test
    @DisplayName("should return HttpStatus.OK and successfully follow user")
    void shouldReturnHttpStatusOkFollowUser() throws Exception {

        setAuth(user1);

        mockMvc.perform(MockMvcRequestBuilders.post(followURI + user3.getUserId()))
                .andExpect(jsonPath("$.message", is("User followed successfully")))
                .andExpect(status().isOk());

        assertTrue(userDao.isFollowing(1L, 3L));
    }

    @Test
    @DisplayName("should return HttpStatus.Bad_Request when following one more time")
    void shouldReturnStatusBadRequestWhenFollowAgain() throws Exception {

        setAuth(user2);

        mockMvc.perform(MockMvcRequestBuilders.post(followURI + user1.getUserId()))
                .andExpect(jsonPath("$.message", is("User already followed")))
                .andExpect(status().isBadRequest());

        assertTrue(userDao.isFollowing(2L, 1L));
    }

    @Test
    @DisplayName("should return HttpStatus.Not_Found when following non existing user")
    void shouldReturnUserNotFoundWhenFollow() throws Exception {

        setAuth(user1);

        mockMvc.perform(MockMvcRequestBuilders.post(followURI + nonExistingUserId))
                .andExpect(jsonPath("$.errorMessage",
                        is("User not found with the given input data : '" + nonExistingUserId +
                        "'")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return HttpStatus.Bad_Request when user following himself")
    void shouldReturnBadRequestWhenFollowHimself() throws Exception {

        setAuth(user1);

        mockMvc.perform(MockMvcRequestBuilders.post(followURI + user1.getUserId()))
                .andExpect(jsonPath("$.errorMessage", is("User cannot follow himself")))
                .andExpect(status().isBadRequest());
    }

    // UNFOLLOW TESTS
    @Test
    @DisplayName("should return HttpStatus.OK when unfollowing")
    void shouldReturnHttpStatusOkWhenUnfollowUser() throws Exception {

        setAuth(user2);

        mockMvc.perform(MockMvcRequestBuilders.delete(unfollowURI + user1.getUserId()))
                .andExpect(status().isNoContent());

        assertFalse(userDao.isFollowing(2L, 1L));
    }

    @Test
    @DisplayName("should return HttpStatus.Bad_Request when unfollowing not following user")
    void shouldReturnHttpStatusBadRequestWhenUnfollowNotFollowing() throws Exception {

        setAuth(user1);

        mockMvc.perform(MockMvcRequestBuilders.delete(unfollowURI + user3.getUserId()))
                .andExpect(jsonPath("$.message", is("User not following")))
                .andExpect(status().isBadRequest());

        assertFalse(userDao.isFollowing(1L, 3L));
    }

    @Test
    @DisplayName("should return HttpStatus.Bad_Request when unfollow himself")
    void shouldReturnBadRequestWhenUnfollowHimself() throws Exception {

        setAuth(user1);

        mockMvc.perform(MockMvcRequestBuilders.delete(unfollowURI + user1.getUserId()))
                .andExpect(jsonPath("$.errorMessage", is("User cannot unfollow himself")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return HttpStatus.Not_Found when unfollow non-existing user")
    void shouldReturnUserNotFoundWhenUnfollow() throws Exception {

        setAuth(user1);

        mockMvc.perform(MockMvcRequestBuilders.delete(unfollowURI + nonExistingUserId))
                .andExpect(status().isNotFound());
    }

    // GET FOLLOWERS TESTS
    @Test
    @DisplayName("should return list of followers")
    void shouldReturnFollowers() throws Exception {

        setAuth(user1);

        mockMvc.perform(MockMvcRequestBuilders.get(followersURI + user1.getUserId()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());

        assertEquals(1, userDao.findFollowers(user1.getUserId()).size(), "User 1 has more followers");
    }

    @Test
    @DisplayName("should return empty list when retrieving followers")
    void shouldReturnEmptyListWhenRetrievingFollowers() throws Exception {

        setAuth(user2);

        mockMvc.perform(MockMvcRequestBuilders.get(followersURI + user2.getUserId()))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk());

        assertEquals(0, userDao.findFollowers(user2.getUserId()).size(), "User 2 has followers");
    }

    @Test
    @DisplayName("should throw HttpStatus.Not_Found when retrieving followers of non-existing user")
    void shouldReturnUserNotFoundWhenRetrievingFollowers() throws Exception {

        setAuth(user2);

        mockMvc.perform(MockMvcRequestBuilders.get(followersURI + nonExistingUserId))
                .andExpect(status().isNotFound());

        assertFalse(userDao.existsById(nonExistingUserId), "User Exists with ID: " + nonExistingUserId);
    }

    // GET FOLLOWINGS TESTS
    @Test
    @DisplayName("should return list of followings")
    void shouldReturnFollowings() throws Exception {

        setAuth(user1);

        mockMvc.perform(MockMvcRequestBuilders.get(followingsURI + user2.getUserId()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());

        assertEquals(1, userDao.findFollowings(user2.getUserId()).size(), "User 2 has more followings");
    }

    @Test
    @DisplayName("should return empty list when retrieving followings")
    void shouldReturnEmptyListWhenRetrievingFollowings() throws Exception {

        setAuth(user2);

        mockMvc.perform(MockMvcRequestBuilders.get(followingsURI + user1.getUserId()))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk());

        assertEquals(0, userDao.findFollowings(user1.getUserId()).size(), "User 1 has followings");
    }

    @Test
    @DisplayName("should throw HttpStatus.Not_Found when retrieving followings of non-existing user")
    void shouldReturnUserNotFoundWhenRetrievingFollowings() throws Exception {

        setAuth(user2);

        mockMvc.perform(MockMvcRequestBuilders.get(followingsURI + nonExistingUserId))
                .andExpect(status().isNotFound());

        assertFalse(userDao.existsById(nonExistingUserId), "User Exists with ID: " + nonExistingUserId);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        jdbcTemplate.execute("DELETE FROM user_follows");
        jdbcTemplate.execute("DELETE FROM users");
    }
}
