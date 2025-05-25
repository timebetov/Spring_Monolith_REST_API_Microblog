package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.user.UserDTO;

import java.util.List;

public interface IFollowService {

    boolean isFollowing(Long currentUser, Long authorId);
    boolean followUser(Long followerId, Long followedId);
    boolean unfollowUser(Long followerId, Long followedId);
    List<UserDTO> getFollowers(Long userId);
    List<UserDTO> getFollowings(Long userId);
}
