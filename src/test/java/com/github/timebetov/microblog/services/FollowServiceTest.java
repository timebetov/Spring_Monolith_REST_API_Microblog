package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.user.UserDTO;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.impl.FollowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FollowServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private FollowService followService;

    /**
     * <h2>isFollowing TESTS</h2>
     */
    @Test
    @DisplayName("should return true when checking if user 1 is following user 2")
    void shouldReturnTrueWhenCheckingIfUser1FollowingUser2() {

        when(userDao.existsById(any(Long.class)))
                .thenReturn(true)
                .thenReturn(true);
        when(userDao.isFollowing(1L, 2L)).thenReturn(true);
        assertTrue(followService.isFollowing(1L, 2L), "User 1 not following User 2");
    }

    @Test
    @DisplayName("should return false when user 1 not following user 2")
    void shouldReturnFalseWhenUserNotFollowing() {

        when(userDao.isFollowing(1L, 2L)).thenReturn(false);
        when(userDao.existsById(any(Long.class)))
                .thenReturn(true)
                .thenReturn(true);
        assertFalse(followService.isFollowing(1L, 2L), "User 1 is following User 2");
    }

    /**
     * <h2>Follow TESTS</h2>
     */

    @Test
    @DisplayName("should return true when following user")
    void shouldReturnTrueWhenUserFollowing() {

        when(userDao.existsById(1L)).thenReturn(true);
        when(userDao.existsById(2L)).thenReturn(true);
        when(userDao.isFollowing(1L, 2L)).thenReturn(false);
        doNothing().when(userDao).insertFollow(1L, 2L);

        assertTrue(followService.followUser(1L, 2L));

        verify(userDao, times(2)).existsById(any(Long.class));
        verify(userDao, times(1)).isFollowing(1L, 2L);
        verify(userDao, times(1)).insertFollow(1L, 2L);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when attempting user to follow himself")
    void shouldThrowIllegalArgumentExceptionWhenFollowUserWithSameId() {

        assertThrows(IllegalArgumentException.class, () -> followService.followUser(1L, 1L));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when attempting to follow non existing user")
    void shouldThrowResourceNotFoundExceptionWhenFollowNonExistingUser() {

        when(userDao.existsById(2L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> followService.followUser(1L, 2L));
        verify(userDao, times(1)).existsById(2L);
    }

    @Test
    @DisplayName("should return false when attempting follow one more time already following user")
    void shouldReturnFalseWhenFollowAlreadyFollowingUser() {

        when(userDao.existsById(any(Long.class)))
                .thenReturn(true)
                .thenReturn(true);

        when(userDao.isFollowing(1L, 2L)).thenReturn(true);

        assertFalse(followService.followUser(1L, 2L), "User 1 not following User 2");

        verify(userDao, times(2)).existsById(any(Long.class));
        verify(userDao, times(1)).isFollowing(1L, 2L);
    }

    /**
     * <h2>UNFOLLOW TESTS</h2>
     */
    @Test
    @DisplayName("should return true when unfollowing user")
    void shouldReturnTrueWhenUnfollowUser() {

        when(userDao.existsById(any(Long.class)))
                .thenReturn(true)
                .thenReturn(true);
        when(userDao.isFollowing(1L, 2L)).thenReturn(true);

        assertTrue(followService.unfollowUser(1L, 2L), "User 1 following User 2");

        verify(userDao, times(2)).existsById(any(Long.class));
        verify(userDao, times(1)).isFollowing(1L, 2L);
        verify(userDao, times(1)).deleteFollow(1L, 2L);
    }

    @Test
    @DisplayName("should return false when unfollowing not following user")
    void shouldReturnFalseWhenUnfollowNotFollowing() {

        when(userDao.existsById(any(Long.class)))
                .thenReturn(true)
                .thenReturn(true);
        when(userDao.isFollowing(1L, 2L)).thenReturn(false);

        assertFalse(followService.unfollowUser(1L, 2L), "User 1 not following User 2");

        verify(userDao, times(2)).existsById(any(Long.class));
        verify(userDao, times(1)).isFollowing(1L, 2L);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException user can't unfollow himself")
    void shouldThrowIllegalArgumentExceptionWhenUnfollowHimself() {

        assertThrows(IllegalArgumentException.class, () -> followService.unfollowUser(1L, 1L));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when unfollowing non-existing user")
    void shouldThrowResourceNotFoundExceptionWhenUnfollowNonExistingUser() {

        when(userDao.existsById(2L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> followService.unfollowUser(1L, 2L));
        verify(userDao, times(1)).existsById(2L);
    }


    /**
     * <h2>getFollowers TESTS</h2>
     */
    @Test
    @DisplayName("should return 2 followers of user")
    void shouldReturnListOfFollowers() {

        User follower1 = User.builder().userId(1L).username("user1").email("user1@test.com").role(User.Role.USER).build();
        User follower2 = User.builder().userId(2L).username("user2").email("user2@test.com").role(User.Role.USER).build();

        when(userDao.existsById(3L)).thenReturn(true);
        when(userDao.findFollowers(3L)).thenReturn(List.of(follower1, follower2));

        List<UserDTO> followers = followService.getFollowers(3L);
        assertNotNull(followers, "Followers list is null");
        assertEquals(2, followers.size(), "User does not have 2 followers");

        verify(userDao, times(1)).existsById(3L);
        verify(userDao, times(1)).findFollowers(3L);
    }

    @Test
    @DisplayName("should return empty list when retrieving followers of user")
    void shouldReturnEmptyListWhenRetrievingFollowers() {

        when(userDao.existsById(3L)).thenReturn(true);
        when(userDao.findFollowers(3L)).thenReturn(List.of());

        List<UserDTO> followers = followService.getFollowers(3L);
        assertNotNull(followers, "Followers list is null");
        assertEquals(0, followers.size(), "User has more followers");

        verify(userDao, times(1)).existsById(3L);
        verify(userDao, times(1)).findFollowers(3L);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when retrieving followers of non-existing user")
    void shouldThrowResourceNotFoundExceptionWhenRetrievingNonExistingUsersFollowers() {

        when(userDao.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> followService.getFollowers(1L));
        verify(userDao, times(1)).existsById(1L);
    }

    /**
     * <h2>getFollowings TESTS</h2>
     */
    @Test
    @DisplayName("should return list of followings")
    void shouldReturnListOfFollowings() {

        User following1 = User.builder().userId(1L).username("user1").email("user1@test.com").role(User.Role.USER).build();
        User following2 = User.builder().userId(2L).username("user2").email("user2@test.com").role(User.Role.USER).build();

        when(userDao.existsById(3L)).thenReturn(true);
        when(userDao.findFollowings(3L)).thenReturn(List.of(following1, following2));

        List<UserDTO> followings = followService.getFollowings(3L);
        assertNotNull(followings, "Following list is null");
        assertEquals(2, followings.size(), "User following no one");

        verify(userDao, times(1)).existsById(3L);
        verify(userDao, times(1)).findFollowings(3L);
    }

    @Test
    @DisplayName("should return empty list when retrieving followings of user")
    void shouldReturnEmptyListWhenRetrievingFollowings() {

        when(userDao.existsById(3L)).thenReturn(true);
        when(userDao.findFollowings(3L)).thenReturn(List.of());

        List<UserDTO> followings = followService.getFollowings(3L);
        assertNotNull(followings, "Following list is null");
        assertEquals(0, followings.size(), "User following more users");

        verify(userDao, times(1)).existsById(3L);
        verify(userDao, times(1)).findFollowings(3L);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when retrieving followings of non-existing user")
    void shouldThrowResourceNotFoundExceptionWhenRetrievingFollowingsNonExistingUser() {

        when(userDao.existsById(3L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> followService.getFollowings(3L));
        verify(userDao, times(1)).existsById(3L);
    }

}
