package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.moment.MomentDTO;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.repository.MomentDao;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.impl.FollowService;
import com.github.timebetov.microblog.services.impl.MomentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class MomentServiceTest {

    @MockitoBean
    private MomentDao momentDao;

    @MockitoBean
    private UserDao userDao;

    @MockitoBean
    private FollowService followService;

    @Autowired
    private MomentService momentService;

    @Mock
    CurrentUserContext currentUser;

    Moment moment;
    User author;

    UUID momentID;

    @BeforeEach
    void setUp() {

        momentID = UUID.randomUUID();

        author = new User();

        moment = Moment.builder()
                .momentId(momentID)
                .build();
    }

    /**
     * <h4>Tests saving a valid moment.</h4>
     *
     * <p><b>Expected:</b> Moment is persisted successfully.</p>
     */
    @Test
    @DisplayName("should save a moment")
    public void shouldSaveMoment() {

        RequestMomentDTO reqMomentDTO = RequestMomentDTO.builder()
                .text("FOR ALL USERS")
                .visibility("PUBLIC")
                .build();

        author.setUserId(1L);
        moment.setAuthor(author);

        moment.setVisibility(Moment.Visibility.PUBLIC);

        when(userDao.findById(1L)).thenReturn(Optional.of(author));
        when(momentDao.save(any(Moment.class))).thenReturn(moment);

        momentService.createMoment(1L, reqMomentDTO);

        ArgumentCaptor<Moment> momentCaptor = ArgumentCaptor.forClass(Moment.class);
        verify(momentDao).save(momentCaptor.capture());
        Moment createdMoment = momentCaptor.getValue();

        assertNotNull(createdMoment, "Created moment should not be null");
        assertEquals("FOR ALL USERS", createdMoment.getText());
        assertEquals(1L, moment.getAuthor().getUserId());
        assertEquals(Moment.Visibility.PUBLIC, moment.getVisibility());

        verify(userDao, times(1)).findById(1L);
        verify(momentDao, times(1)).save(any(Moment.class));
    }

    @Test
    @DisplayName("should save new moment when visibility type not defined")
    public void shouldSaveNewMomentWhenVisibilityTypeNotDefined() {

        RequestMomentDTO req = RequestMomentDTO.builder()
                .text("FOR ALL USERS")
                .build();

        author.setUserId(1L);
        moment.setAuthor(author);
        moment.setVisibility(Moment.Visibility.PUBLIC);

        when(userDao.findById(1L)).thenReturn(Optional.of(author));
        when(momentDao.save(any(Moment.class))).thenReturn(moment);

        momentService.createMoment(1L, req);
        ArgumentCaptor<Moment> momentCaptor = ArgumentCaptor.forClass(Moment.class);
        verify(momentDao).save(momentCaptor.capture());
        Moment createdMoment = momentCaptor.getValue();
        assertNotNull(createdMoment, "Created moment should not be null");
        assertEquals("FOR ALL USERS", createdMoment.getText());
        assertEquals(1L, moment.getAuthor().getUserId());
        assertEquals(Moment.Visibility.PUBLIC, moment.getVisibility());
        verify(userDao, times(1)).findById(1L);
        verify(momentDao, times(1)).save(any(Moment.class));
    }

    /**
     * <h4>Tests saving a moment fails when giving non-existing author ID.</h4>
     *
     * <p><b>Expected:</b> Throws {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException}.</p>
     *
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when saving moment with non-existing authorId")
    public void shouldThrowExceptionWhenSavingMomentWithNonExistingAuthorId() {

        when(userDao.findById(100L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.createMoment(100L, new RequestMomentDTO()));
        verify(userDao, times(1)).findById(100L);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/author_moment_visibility_test_cases.csv", numLinesToSkip = 1)
    @DisplayName("should return moment based on access")
    void shouldReturnMomentBasedOnAccessRulesWhenAuthorRetrieving(
            String visibility,
            boolean isAdmin,
            boolean isAuthor,
            boolean isFollower,
            boolean shouldAccess
    ) {

        Long currentUserId = 1L;
        Long authorId = isAuthor ? currentUserId : 2L;

        moment.setVisibility(Moment.Visibility.valueOf(visibility));
        author.setUserId(authorId);
        moment.setAuthor(author);

        when(currentUser.getUserId()).thenReturn(currentUserId);
        when(currentUser.isAdmin()).thenReturn(isAdmin);
        when(momentDao.findById(momentID)).thenReturn(Optional.of(moment));
        when(userDao.findById(authorId)).thenReturn(Optional.of(author));
        when(followService.isFollowing(currentUserId, authorId)).thenReturn(isFollower);

        if (shouldAccess) {
            MomentDTO foundMoment = momentService.getMomentById(momentID, currentUser);
            assertNotNull(foundMoment, "Moment should not be null");
            verify(momentDao).findById(momentID);
        } else {
            assertThrows(AccessDeniedException.class, () -> momentService.getMomentById(momentID, currentUser));
        }
    }

    /**
     * <h4>Tests that retrieving a moment with valid ID but not existing one throws an exception</h4>
     *
     * <p><b>Expected:</b> {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException} is thrown.</p>
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when retrieving non-existing moment")
    public void shouldThrowResourceNotFoundExceptionWhenRetrievingMomentWithNonExistingId() {

        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.getMomentById(UUID.randomUUID(), currentUser));
        verify(momentDao, times(1)).findById(any(UUID.class));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/author_moment_visibility_test_cases.csv", numLinesToSkip = 1)
    @DisplayName("should return moments based on access rules in case authorId != null")
    void shouldReturnMomentsBaseOnAccessRulesWhenAuthorIdNotNull(
            String visibility,
            boolean isAdmin,
            boolean isAuthor,
            boolean isFollower,
            boolean shouldAccess
    ) {

        Long currentUserId = 1L;
        Long authorId = isAuthor ? currentUserId : 2L;

        when(currentUser.getUserId()).thenReturn(currentUserId);
        when(currentUser.isAdmin()).thenReturn(isAdmin);

        moment.setVisibility(Moment.Visibility.valueOf(visibility));
        author.setUserId(authorId);
        moment.setAuthor(author);

        when(momentDao.findMomentByAuthor_UserId(authorId)).thenReturn(List.of(moment));
        when(userDao.findById(authorId)).thenReturn(Optional.of(author));
        when(followService.isFollowing(currentUserId, authorId)).thenReturn(isFollower);

        List<MomentDTO> result = momentService.getMoments(authorId, visibility, currentUser);
        if (shouldAccess) {
            assertEquals(1, result.size(), "User should see the moment");
        } else {
            assertEquals(0, result.size(), "User should NOT see the moment");
        }

        verify(momentDao).findMomentByAuthor_UserId(authorId);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/moment_visibility_test_cases.csv", numLinesToSkip = 1)
    @DisplayName("should return moments based on access rules in case authorId = null")
    void shouldReturnMomentsBasedOnAccessRulesWhenAuthorIdNull(
            String visibility,
            boolean isAdmin,
            boolean isFollower,
            boolean expectedAccess
    ) {
        Long currentUserId = 1L;
        Long momentAuthorId = 2L;

        when(currentUser.getUserId()).thenReturn(currentUserId);
        when(currentUser.isAdmin()).thenReturn(isAdmin);

        moment.setVisibility(Moment.Visibility.valueOf(visibility));
        author.setUserId(momentAuthorId);
        moment.setAuthor(author);

        when(momentDao.findAll()).thenReturn(List.of(moment));
        when(followService.isFollowing(currentUserId, momentAuthorId)).thenReturn(isFollower);

        List<MomentDTO> result = momentService.getMoments(null, visibility, currentUser);
        if (expectedAccess) {
            assertEquals(1, result.size(), "User should see the moment");
        } else {
            assertEquals(0, result.size(), "User should NOT see the moment");
        }

        verify(momentDao).findAll();
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when currentUser is null")
    void shouldThrowIllegalArgumentExceptionWhenCurrentUserIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> momentService.getMoments(null, null ,null),
                "Expected Exception when user is not logged in"
        );
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when author not found")
    void shouldThrowResourceNotFoundExceptionWhenAuthorNotFound() {

        Long nonExistingUserId = 99L;
        when(userDao.findById(nonExistingUserId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> momentService.getMoments(nonExistingUserId, null, CurrentUserContext.builder().userId(1L).build()),
                "Expected exception when author not found"
        );
    }

    /**
     * <h4>Tests that a moment is updated and saved correctly.</h4>
     * <p>
     *     Only <i>Admin</i>s and <i>Author</i>s themselves have permissions to perform updating.
     * </p>
     *
     * <ul>
     *     <li><b>Given:</b> An existing moment and a valid {@link RequestMomentDTO} update payload.</li>
     *     <li><b>When:</b> The {@code updateMoment} method is called.</li>
     *     <li><b>Then:</b> The moment should be updated with the new data and persisted.</li>
     * </ul>
     */
    @ParameterizedTest
    @DisplayName("should update and save moment based on access rules")
    @MethodSource("accessCases")
    void shouldUpdateMomentBasedOnAccessRules(boolean isAuthor, boolean isAdmin, boolean canAccess) {

        Long authorId = 10L;
        Long currentUserId = isAuthor ? authorId : 20L;
        author.setUserId(authorId);
        moment.setAuthor(author);

        RequestMomentDTO requestMomentDTO = RequestMomentDTO.builder()
                .text("updated text".toUpperCase())
                .visibility("DRAFT")
                .build();

        when(currentUser.getUserId()).thenReturn(currentUserId);
        when(currentUser.isAdmin()).thenReturn(isAdmin);

        when(momentDao.findById(momentID)).thenReturn(Optional.of(moment));

        if (canAccess) {
            assertDoesNotThrow(() -> momentService.updateMoment(momentID, requestMomentDTO, currentUser));
            verify(momentDao, times(1)).findById(momentID);
            verify(momentDao, times(1)).save(moment);
        } else {
            assertThrows(AccessDeniedException.class, () -> momentService.updateMoment(momentID, requestMomentDTO, currentUser));
            verify(momentDao, never()).save(moment);
        }
    }

    /**
     * <h4>Tests updating with valid but non-existing moment. Throws an Exception</h4>
     *
     * <p><b>Expected:</b> {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException} is thrown.</p>
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when attempting update non-existing moment")
    public void shouldThrowResourceNotFoundExceptionWhenUpdatingMomentWithNonExistingId() {

        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.updateMoment(UUID.randomUUID(), null, currentUser));
        verify(momentDao, times(1)).findById(any(UUID.class));
    }

    /**
     * <h4>Tests that a moment is successfully deleted when a valid and existing ID is provided.</h4>
     * <p>
     *     Only <i>Admin</i>s and <i>Author</i>s themselves have permissions to perform deletion.
     * </p>
     *
     * <p><b>Expected:</b> After deletion, attempting to retrieve the moment results in {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException}.</p>
     */
    @ParameterizedTest
    @DisplayName("should delete moment based on access rules")
    @MethodSource("accessCases")
    void shouldDeleteMomentBasedOnAccessRules(boolean isAuthor, boolean isAdmin, boolean canAccess) {

        Long authorId = 10L;
        Long currentUserId = isAuthor ? authorId : 20L;
        author.setUserId(authorId);
        moment.setAuthor(author);

        when(currentUser.getUserId()).thenReturn(currentUserId);
        when(currentUser.isAdmin()).thenReturn(isAdmin);

        when(momentDao.findById(momentID))
                .thenReturn(Optional.of(moment))
                .thenReturn(Optional.empty());
        if (canAccess) {
            assertDoesNotThrow(() -> momentService.deleteMoment(momentID, currentUser));
            verify(momentDao, times(1)).findById(momentID);
            verify(momentDao, times(1)).delete(moment);
            assertThrows(ResourceNotFoundException.class, () -> momentService.deleteMoment(momentID, currentUser));
        } else {
            assertThrows(AccessDeniedException.class, () -> momentService.deleteMoment(momentID, currentUser));
            verify(momentDao, never()).delete(moment);
        }
    }

    /**
     * <h4>Tests deletion with valid but not existing moment. Throws an Exception</h4>
     *
     * <p><b>Expected:</b> {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException} is thrown.</p>
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when attempting non-existing moment")
    public void shouldThrowResourceNotFoundExceptionWhenDeletingMomentWithNonExistingId() {

        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.deleteMoment(UUID.randomUUID(), currentUser));
        verify(momentDao, times(1)).findById(any(UUID.class));
    }


    private static Stream<Arguments> accessCases() {
        return Stream.of(
                Arguments.of(true, false, true),
                Arguments.of(false, true, true),
                Arguments.of(true, true, true),
                Arguments.of(false, false, false)
        );
    }

}
