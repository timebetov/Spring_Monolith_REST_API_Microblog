package com.github.timebetov.microblog.services.impl;

import com.github.timebetov.microblog.dtos.user.UserDTO;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import com.github.timebetov.microblog.mappers.UserMapper;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.IFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService implements IFollowService {

    private final UserDao userDao;

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {

        if (!userDao.existsById(followedId)) {
            throw new ResourceNotFoundException("User", "followedId", String.valueOf(followedId));
        }
        if (!userDao.existsById(followerId)) {
            throw new ResourceNotFoundException("User", "followerId", String.valueOf(followerId));
        }

        return followedId.equals(followerId) || userDao.isFollowing(followerId, followedId);
    }

    @Override
    public boolean followUser(Long followerId, Long followedId) {

        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Follower id cannot be the same as follower id");
        }

        if (!userDao.existsById(followedId)) {
            throw new ResourceNotFoundException("User", "followedId", String.valueOf(followedId));
        }
        if (!userDao.existsById(followerId)) {
            throw new ResourceNotFoundException("User", "followerId", String.valueOf(followerId));
        }
        if (userDao.isFollowing(followerId, followedId)) {
            return false;
        }

        userDao.insertFollow(followerId, followedId);
        return true;
    }

    @Override
    public boolean unfollowUser(Long followerId, Long followedId) {

        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Follower id cannot be the same as follower id");
        }
        if (!userDao.existsById(followedId)) {
            throw new ResourceNotFoundException("User", "followedId", String.valueOf(followedId));
        }
        if (!userDao.existsById(followerId)) {
            throw new ResourceNotFoundException("User", "followerId", String.valueOf(followerId));
        }
        if (!userDao.isFollowing(followerId, followedId)) {
            return false;
        }

        userDao.deleteFollow(followerId, followedId);
        return true;
    }

    @Override
    public List<UserDTO> getFollowers(Long userId) {

        if (!userDao.existsById(userId)) {
            throw new ResourceNotFoundException("User", "userId", String.valueOf(userId));
        }

        return userDao.findFollowers(userId)
                .stream()
                .map(UserMapper::mapToUserDTO)
                .toList();
    }

    @Override
    public List<UserDTO> getFollowings(Long userId) {

        if (!userDao.existsById(userId)) {
            throw new ResourceNotFoundException("User", "userId", String.valueOf(userId));
        }

        return userDao.findFollowings(userId)
                .stream()
                .map(UserMapper::mapToUserDTO)
                .toList();
    }
}
