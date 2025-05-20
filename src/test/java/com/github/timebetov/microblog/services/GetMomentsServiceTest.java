package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.moment.MomentDTO;
import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.repository.MomentDao;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.impl.FollowService;
import com.github.timebetov.microblog.services.impl.MomentService;
import org.junit.jupiter.api.DisplayName;
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

    @ParameterizedTest
    @CsvFileSource(resources = "/author_moment_visibility_test_cases.csv", numLinesToSkip = 1)
    @DisplayName("should return moments based on access rules")
    void getMomentsAccess(
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


        Moment moment = Moment.builder()
                .momentId(UUID.randomUUID())
                .visibility(Moment.Visibility.valueOf(visibility))
                .build();

        User author = new User();
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
}
