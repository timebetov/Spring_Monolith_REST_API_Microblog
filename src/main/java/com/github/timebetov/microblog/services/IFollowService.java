package com.github.timebetov.microblog.services;

public interface IFollowService {

    boolean isFollowing(Long currentUser, Long authorId);
    /* TODO: Implement in future
    boolean followUser(Long followerId, Long followedId);
    boolean unfollowUser(Long followerId, Long followedId);
    List<UserDTO> getFollowers(Long userId);
    List<UserDTO> getFollowings(Long userId);
     */
}
