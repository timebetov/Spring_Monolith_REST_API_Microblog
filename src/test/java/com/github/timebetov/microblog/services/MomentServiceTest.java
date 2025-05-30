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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MomentServiceTest {

    @Mock
    private MomentDao momentDao;

    @Mock
    private UserDao userDao;

    @Mock
    private FollowService followService;

    @InjectMocks
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
    void shouldSaveMoment() {

        RequestMomentDTO reqMomentDTO = RequestMomentDTO.builder()
                .text("FOR ALL USERS")
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

    /**
     * <h4>Tests saving a moment fails when giving non-existing author ID.</h4>
     *
     * <p><b>Expected:</b> Throws {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException}.</p>
     *
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when saving moment with non-existing authorId")
    void shouldThrowResourceNotFoundExceptionWhenSavingMomentWithNonExistingAuthorId() {

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
        if (!visibility.equals(Moment.Visibility.PUBLIC.toString())) {
            when(currentUser.isAdmin()).thenReturn(isAdmin);
        }
        when(momentDao.findById(momentID)).thenReturn(Optional.of(moment));
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
     * <h4>Tests that retrieving a moment with valid ID but not existing one, throws an exception</h4>
     *
     * <p><b>Expected:</b> {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException} is thrown.</p>
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when retrieving non-existing moment")
    void shouldThrowResourceNotFoundExceptionWhenRetrievingMomentById() {

        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.getMomentById(UUID.randomUUID(), currentUser));
        verify(momentDao, times(1)).findById(any(UUID.class));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when retrieving moment by id with null current user")
    void shouldThrowIllegalArgumentExceptionWhenRetrievingMomentByIdWithNullCurrentUser() {

        assertThrows(IllegalArgumentException.class, () -> momentService.getMomentById(UUID.randomUUID(), null));
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
        if (!visibility.equals(Moment.Visibility.PUBLIC.toString())) {
            when(currentUser.isAdmin()).thenReturn(isAdmin);
        }

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
        if (!visibility.equals(Moment.Visibility.PUBLIC.toString())) {
            when(currentUser.isAdmin()).thenReturn(isAdmin);
        }

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

    @Test
    void shouldUpdateMomentAndSave() {

        RequestMomentDTO momentDetails = RequestMomentDTO.builder()
                .text("UPDATED TEXT")
                .build();
        when(momentDao.findById(momentID)).thenReturn(Optional.of(moment));
        assertDoesNotThrow(() -> momentService.updateMoment(momentID, momentDetails, author.getUserId()));
        verify(momentDao, times(1)).findById(momentID);
        verify(momentDao, times(1)).save(any(Moment.class));
    }

    /**
     * <h4>Tests updating with valid but non-existing moment. Throws an Exception</h4>
     *
     * <p><b>Expected:</b> {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException} is thrown.</p>
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when attempting update non-existing moment")
    void shouldThrowResourceNotFoundExceptionWhenUpdatingMomentWithNonExistingId() {

        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.updateMoment(UUID.randomUUID(), null, null));
        verify(momentDao, times(1)).findById(any(UUID.class));
    }

    @Test
    void shouldDeleteMomentById() {

        UUID id = UUID.randomUUID();

        doNothing().when(momentDao).deleteById(id);
        assertDoesNotThrow(() -> momentService.deleteMoment(id, 1L));
        verify(momentDao, times(1)).deleteById(id);
    }

    /**
     * <h4>Tests deletion with valid but not existing moment. Throws an Exception</h4>
     *
     * <p><b>Expected:</b> {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException} is thrown.</p>
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when retrieving moment author id")
    void shouldThrowResourceNotFoundExceptionWhenRetrievingMomentAuthorIdWithNonExistingId() {

        when(momentDao.findAuthorIdByMomentId(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.getAuthorId(UUID.randomUUID()));
        verify(momentDao, times(1)).findAuthorIdByMomentId(any(UUID.class));
    }

    @Test
    @DisplayName("should return author id when retrieving by moment id")
    void shouldReturnAuthorIdOfMomentById() {

        UUID id = UUID.randomUUID();

        when(momentDao.findAuthorIdByMomentId(id)).thenReturn(Optional.of(1L));
        assertEquals(1L, momentService.getAuthorId(id));
        verify(momentDao, times(1)).findAuthorIdByMomentId(id);
    }
}
