package com.github.timebetov.microblog.services.impl;

import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.IFollowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService implements IFollowerService {

    private final UserDao userDao;

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        return followedId.equals(followerId) || userDao.isFollowing(followerId, followedId);
    }
}
