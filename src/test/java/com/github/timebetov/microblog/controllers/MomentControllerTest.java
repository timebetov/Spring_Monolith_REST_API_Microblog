package com.github.timebetov.microblog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.models.UserDetailsImpl;
import com.github.timebetov.microblog.repository.MomentDao;
import com.github.timebetov.microblog.repository.UserDao;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MomentControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MomentDao momentDao;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDao userDao;

    UserDetailsImpl user;
    UserDetailsImpl admin;

    @BeforeEach
    void init() {

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(MockMvcRequestBuilders.get("/").contextPath("/api"))
                .build();

        jdbcTemplate.execute("INSERT INTO users (user_id, email, username, password, role, created_at, created_by) " +
                "VALUES (1, 'user1@test.com', 'user1', 'user1PWD', 'USER', CURRENT_TIMESTAMP, 'SYSTEM')");
        jdbcTemplate.execute("INSERT INTO users (user_id, email, username, password, role, created_at, created_by) " +
                "VALUES (2, 'admin@test.com', 'admin', 'adminPWD', 'ADMIN', CURRENT_TIMESTAMP, 'SYSTEM')");
        jdbcTemplate.execute("INSERT INTO moments (moment_id, text, visibility, author_id, created_at, created_by) " +
                "VALUES ('1ee1704a-f1af-474a-b18c-04bfd0210865', 'Lorem ipsum plain text', 'PUBLIC', 2, CURRENT_TIMESTAMP, 'SYSTEM')");
        jdbcTemplate.execute("INSERT INTO moments (moment_id, text, visibility, author_id, created_at, created_by) " +
                "VALUES ('ee3e6649-5f75-45fb-a492-a5e037b8f545', 'Lorem ipsum plain text', 'DRAFT', 2, CURRENT_TIMESTAMP, 'SYSTEM')");
    }

    @BeforeEach
    void setAuth() {
        user = new UserDetailsImpl(1L, "user1", "user1@test.com", "user1PWD", "USER");
        admin = new UserDetailsImpl(100L, "admin", "admin@test.com", "adminPWD", "ADMIN");

        Authentication authentication = new TestingAuthenticationToken(user, null, "ROLE_USER");
        authentication.setAuthenticated(true);

        SecurityContext secContext = SecurityContextHolder.createEmptyContext();
        secContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(secContext);
    }

    /**
     * Test case for the POST /api/moments/ endpoint.
     * <p>This test verifies the behavior when attempting to create a valid Moment.</p>
     *
     * <ul>
     *     <li><b>Expected:</b> The API should return HTTP 201 CREATED</li>
     *     <li>It should also include a successful message in the response JSON.</li>
     *     <li>New Moment should be persisted to the database.</li>
     * </ul>
     *
     * Preconditions:
     * - There should be exactly 1 Moment in the DB before this test runs.
     */
    @Test
    @DisplayName("should save a new moment with valid data")
    void shouldSaveMoment() throws Exception {

        RequestMomentDTO req = RequestMomentDTO.builder()
                .text("Some plain text from a new moment with PUBLIC visibility")
                .visibility("PUBLIC")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/moments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        List<Moment> moments = (List<Moment>) momentDao.findAll();
        Assertions.assertEquals(3, moments.size());
    }

    /**
     *
     * Test case for the POST /api/moments/ endpoint.
     * <p>This test verifies the behavior when attempting to create a Moment with Invalid enum type of `Visibility` field.</p>
     *
     * <ul>
     *     <li><b>Expected:</b> The API should return HTTP 400 BAD REQUEST.</li>
     *     <li>It should also include a specific validation message in the response JSON.</li>
     *     <li>No new Moment should be persisted to the database.</li>
     * </ul>
     *
     * Preconditions:
     * - There should already be exactly 1 Moment in the DB before this test runs.
     */

    @Test
    @DisplayName("should not create moment invalid visibility type")
    void shouldNotSaveMomentInvalidVisibilityType() throws Exception {

        RequestMomentDTO dto = RequestMomentDTO.builder()
                .text("Some plain text from a new moment")
                .visibility("NONE")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/moments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(jsonPath("$.errorDetails.visibility", is("Value must be one of: FOLLOWERS_ONLY, DRAFT, PUBLIC")))
                .andExpect(status().isBadRequest());

        List<Moment> moments = (List<Moment>) momentDao.findAll();
        Assertions.assertEquals(2, moments.size());
    }

    /**
     * Test case for the POST /api/moments/ endpoint.
     * <p>This test verifies the behavior when attempting to create a Moment with a null or missing `text` field.</p>
     *
     * <ul>
     *      <li><b>Expected:</b> The API should return HTTP 400 BAD REQUEST.</li>
     *      <li>It should also include a specific validation message in the response JSON.</li>
     *      <li>No new Moment should be saved to the database.</li>
     * </ul>
     *
     * Preconditions:
     * - There should already be exactly 1 Moment in the DB before this test runs
     */
    @Test
    @DisplayName("should not save moment due to text null")
    void shouldNotSaveMomentNullText() throws Exception {

        RequestMomentDTO dto = RequestMomentDTO.builder()
                .visibility("DRAFT")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/moments/")
                        .queryParam("authorId", String.valueOf(2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(jsonPath("$.errorDetails.text", is("Text must be not empty")))
                .andExpect(status().isBadRequest());

        List<Moment> moments = (List<Moment>) momentDao.findAll();
        Assertions.assertEquals(2, moments.size());
    }

    /**
     * Test case for the POST /api/moments/ endpoint.
     * <p>This test verifies the behavior when attempting to create a Moment with too long characters of `text` field</p>
     *
     * <ul>
     *      <li><b>Expected:</b> The API should return HTTP 400 BAD REQUEST.</li>
     *      <li>It should also include a specific validation message in the response JSON.</li>
     *      <li>No new Moment should be saved to the database.</li>
     * </ul>
     * Preconditions:
     * - There should already be exactly 1 Moment in the DB before this test runs
     */
    @Test
    @DisplayName("should not save moment due to text size too long")
    void shouldNotSaveMomentTooLongTextSize() throws Exception {

        RequestMomentDTO dto = RequestMomentDTO.builder()
                .text("A".repeat(501))
                .visibility("DRAFT")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/moments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(jsonPath("$.errorDetails.text", is("Text must contain only 500 characters")))
                .andExpect(status().isBadRequest());

        List<Moment> moments = (List<Moment>) momentDao.findAll();
        Assertions.assertEquals(2, moments.size());
    }

    @Test
    @DisplayName("should return all PUBLIC moments when requesting without defining author id and visibility")
    void shouldReturnPublicMomentsWhenRequestingNullAuthorIdAndVisibility() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("should return moments when retrieving with given author id and visibility")
    void shouldReturnMomentsWhenRetrievingWithGivenAuthorIdAndVisibility() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/")
                        .queryParam("authorId", String.valueOf(2))
                        .queryParam("visibility", "PUBLIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND when retrieving with not existing author id")
    void shouldReturnAuthorNotFoundWhenRetrievingMoments() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/")
                        .queryParam("authorId", String.valueOf(99)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return HttpStatus.BAD_REQUEST when retrieving with not valid visibility type")
    void shouldReturnInvalidVisibilityBadRequestWhenRetrievingMoments() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/")
                        .queryParam("visibility", "NONE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return empty list when retrieving drafts of another user")
    void shouldReturnEmptyListWhenRetrievingDraftsOfAnotherUser() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/")
                        .queryParam("visibility", "DRAFT"))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return HttpStatus.BAD_REQUEST when retrieving with invalid author id")
    void shouldReturnInvalidAuthorIdBadRequestWhenRetrievingMoments() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/")
                        .queryParam("authorId", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return moment from db")
    void shouldReturnMomentFromDb() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/1ee1704a-f1af-474a-b18c-04bfd0210865"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorId", is(2)))
                .andExpect(jsonPath("$.visibility", is("PUBLIC")))
                .andExpect(jsonPath("$.id", is("1ee1704a-f1af-474a-b18c-04bfd0210865")));
    }

    @Test
    @DisplayName("should return HttpStatus.INTERNAL_SERVER_ERROR when retrieving moment by invalid uuid format")
    void shouldReturnInternalServerErrorWhenRetrievingMomentInvalidUUId() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/adsda"))
                .andExpect(jsonPath("$.errorMessage", is("Invalid UUID string: adsda")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND when retrieving non-existing moment by id")
    void shouldReturnMomentNotFoundWhenRetrievingMomentById() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/1ee1304a-f1af-474a-b18c-03cfd0210845"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return HttpStatus.OK, update and save moment in db")
    void shouldUpdateAndSaveMoment() throws Exception {

        // Authenticate as ADMIN
        SecurityContextHolder.clearContext();

        Authentication authentication = new TestingAuthenticationToken(admin, null, admin.getAuthorities());
        authentication.setAuthenticated(true);

        SecurityContext secContext = SecurityContextHolder.createEmptyContext();
        secContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(secContext);

        RequestMomentDTO req = RequestMomentDTO.builder()
                .text("UPDATED TEXT")
                .visibility("DRAFT")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/moments/1ee1704a-f1af-474a-b18c-04bfd0210865")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/1ee1704a-f1af-474a-b18c-04bfd0210865"))
                .andExpect(jsonPath("$.text", is("UPDATED TEXT")))
                .andExpect(jsonPath("$.visibility", is("DRAFT")));
    }

    @Test
    @DisplayName("should not update cause neither author nor admin")
    void shouldNotUpdateMomentCauseNeitherAuthorOrAdmin() throws Exception {

        RequestMomentDTO req = RequestMomentDTO.builder()
                .text("UPDATED TEXT")
                .visibility("DRAFT")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/moments/1ee1704a-f1af-474a-b18c-04bfd0210865")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return HttpStatus.BAD_REQUEST, when updating requesting invalid visibility type")
    void shouldReturnInvalidVisibilityBadRequestWhenUpdatingMoment() throws Exception {

        RequestMomentDTO req = RequestMomentDTO.builder()
                .text("UPDATED TEXT")
                .visibility("NOT VALID")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/moments/1ee1704a-f1af-474a-b18c-04bfd0210865")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return HttpStatus.NOT_FOUND when updating non existing moment")
    void shouldReturnMomentNotFoundWhenUpdating() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.put("/api/moments/1ee1704a-f1af-474a-b18c-04bfd0210825")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RequestMomentDTO.builder().text("JUST TEXT").build())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should not update and save moment due to text size too long")
    void shouldNotUpdateMomentTooLongTextSize() throws Exception {

        RequestMomentDTO dto = RequestMomentDTO.builder()
                .text("A".repeat(501))
                .visibility("DRAFT")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/moments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(jsonPath("$.errorDetails.text", is("Text must contain only 500 characters")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should delete moment by id")
    void shouldDeleteMomentById() throws Exception {

        // Authenticate as ADMIN
        SecurityContextHolder.clearContext();

        Authentication authentication = new TestingAuthenticationToken(admin, null, admin.getAuthorities());
        authentication.setAuthenticated(true);

        SecurityContext secContext = SecurityContextHolder.createEmptyContext();
        secContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(secContext);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/moments/1ee1704a-f1af-474a-b18c-04bfd0210865"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should not delete cause neither author nor admin")
    void shouldNotDeleteMomentCauseNeitherAuthorOrAdmin() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/moments/1ee1704a-f1af-474a-b18c-04bfd0210865"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should throw HttpStatus.NOT_FOUND when deleting moment by id")
    void shouldThrowMomentNotFoundWhenDeleting() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/moments/1ee1704a-f1af-474a-b18c-04bfd0210825"))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        jdbcTemplate.execute("DELETE FROM moments");
        jdbcTemplate.execute("DELETE FROM users");
    }
}
