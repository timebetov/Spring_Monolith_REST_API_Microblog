package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.moment.MomentDTO;
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
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class GetMomentsServiceTest {

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

    @BeforeEach
    void setUp() {
        moment = Moment.builder()
                .momentId(UUID.randomUUID())
                .build();
        author = new User();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/author_moment_visibility_test_cases.csv", numLinesToSkip = 1)
    @DisplayName("should return moments based on access rules in case authorId != null")
    void shouldReturnMomentsBaseOnAccessRulesWhenAuthorIdNotNull(
            String visibility,
            boolean isAdmin,
            boolean isAuthor,
            boolean isFollower,
            boolean expectedVisible
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
        if (expectedVisible) {
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

}
