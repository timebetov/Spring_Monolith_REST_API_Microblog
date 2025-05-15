package com.github.timebetov.microblog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.repository.MomentDao;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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

    @BeforeEach
    void init() {

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(MockMvcRequestBuilders.get("/").contextPath("/api"))
                .build();

        jdbcTemplate.execute("INSERT INTO users (user_id, email, username, password, role, created_at, created_by) " +
                "VALUES (2, 'admin@test.com', 'admin', 'adminPWD', 'ADMIN', CURRENT_TIMESTAMP, 'SYSTEM')");
        jdbcTemplate.execute("INSERT INTO moments (moment_id, text, visibility, author_id, created_at, created_by) " +
                "VALUES ('1ee1704a-f1af-474a-b18c-04bfd0210865', 'Lorem ipsum plain text', 'PUBLIC', 2, CURRENT_TIMESTAMP, 'SYSTEM')");
    }

    /**
     * Test case for the POST /api/moments/ endpoint.
     * <p>This test verifies the behavior when attempting to create a valid Moment.</p>
     *
     * <ul>
     *     <li>Expected: The API should return HTTP 201 CREATED</li>
     *     <li>It should also include a successful message in the response JSON.</li>
     *     <li>New Moment should be persisted to the database.</li>
     * </ul>
     *
     * Preconditions:
     * - authorId = 2, must be existed in DB
     * - There should be exactly 1 Moment in the DB before this test runs.
     */
    @Test
    @DisplayName("shouldCreateNewMoment")
    void createMomentTest() throws Exception {

        RequestMomentDTO req = RequestMomentDTO.builder()
                .text("Some plain text from a new moment with PUBLIC visibility")
                .visibility("PUBLIC")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/moments/")
                        .queryParam("authorId", String.valueOf(2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        List<Moment> moments = (List<Moment>) momentDao.findAll();
        Assertions.assertEquals(2, moments.size());
    }

    /**
     * Test case for the POST /api/moments/ endpoint.
     * <p>This test verifies the behavior when attempting to create a valid Moment with not valid Author ID</p>
     *
     * <ul>
     *     <li>Expected: The API should return HTTP 404 NOT FOUND</li>
     *     <li>It should also include a specific validation message in the response JSON.</li>
     *     <li>No new Moment should be persisted to the database.</li>
     * </ul>
     *
     * Preconditions:
     * - authorId = 1040 must be not persisted in DB
     * - There should already be exactly 1 Moment in the DB before this test runs.
     */
    @Test
    @DisplayName("shouldNotCreateMomentInvalidAuthorId")
    void createMomentInvalidAuthorIdTest() throws Exception {

        RequestMomentDTO req = RequestMomentDTO.builder()
                .text("Some plain text from a new moment with PUBLIC visibility")
                .visibility("PUBLIC")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/moments/")
                        .queryParam("authorId", String.valueOf(1040))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        List<Moment> moments = (List<Moment>) momentDao.findAll();
        Assertions.assertEquals(1, moments.size());
    }

    /**
     *
     * Test case for the POST /api/moments/ endpoint.
     * <p>This test verifies the behavior when attempting to create a Moment with Invalid enum type of `Visibility` field.</p>
     *
     * <ul>
     *     <li>Expected: The API should return HTTP 400 BAD REQUEST.</li>
     *     <li>It should also include a specific validation message in the response JSON.</li>
     *     <li>No new Moment should be persisted to the database.</li>
     * </ul>
     *
     * Preconditions:
     * - authorId = 2 must be valid and present in the DB
     * - There should already be exactly 1 Moment in the DB before this test runs.
     */

    @Test
    @DisplayName("shouldNotCreateMomentInvalidVisibilityType")
    void createMomentInvalidTypeVisibilityTest() throws Exception {

        RequestMomentDTO dto = RequestMomentDTO.builder()
                .text("Some plain text from a new moment")
                .visibility("NONE")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/moments/")
                        .queryParam("authorId", String.valueOf(2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(jsonPath("$.errorDetails.visibility", is("Value must be one of: FOLLOWERS_ONLY, DRAFT, PUBLIC")))
                .andExpect(status().isBadRequest());

        List<Moment> moments = (List<Moment>) momentDao.findAll();
        Assertions.assertEquals(1, moments.size());
    }

    /**
     * Test case for the POST /api/moments/ endpoint.
     * <p>This test verifies the behavior when attempting to create a Moment with a null or missing `text` field.</p>
     *
     * <ul>
     *      <li>Expected: The API should return HTTP 400 BAD REQUEST.</li>
     *      <li>It should also include a specific validation message in the response JSON.</li>
     *      <li>No new Moment should be saved to the database.</li>
     * </ul>
     *
     * Preconditions:
     * - authorId = 2 must be valid and present in the DB
     * - There should already be exactly 1 Moment in the DB before this test runs
     */
    @Test
    @DisplayName("shouldNotCreateMomentWithNullText")
    void createMomentNullTextTest() throws Exception {

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
        Assertions.assertEquals(1, moments.size());
    }

    /**
     * Test case for the POST /api/moments/ endpoint.
     * <p>This test verifies the behavior when attempting to create a Moment with too long characters of `text` field</p>
     *
     * <ul>
     *      <li>Expected: The API should return HTTP 400 BAD REQUEST.</li>
     *      <li>It should also include a specific validation message in the response JSON.</li>
     *      <li>No new Moment should be saved to the database.</li>
     * </ul>
     * Preconditions:
     * - authorId = 2 must be valid and present in the DB
     * - There should already be exactly 1 Moment in the DB before this test runs
     */
    @Test
    @DisplayName("shouldNotCreateMomentTooLongTextSize")
    void createMomentTooLongTextSizeTest() throws Exception {

        RequestMomentDTO dto = RequestMomentDTO.builder()
                .text("A".repeat(501))
                .visibility("DRAFT")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/moments/")
                        .queryParam("authorId", String.valueOf(2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(jsonPath("$.errorDetails.text", is("Text must contain only 500 characters")))
                .andExpect(status().isBadRequest());

        List<Moment> moments = (List<Moment>) momentDao.findAll();
        Assertions.assertEquals(1, moments.size());
    }

    /**
     *
     * TODO: Implement actual method
     */
    @Test
    @DisplayName("shouldGetAllMoments")
    void getAllMomentsTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/moments/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM moments");
        jdbcTemplate.execute("DELETE FROM users");
    }
}
