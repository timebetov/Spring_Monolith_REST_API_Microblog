package com.github.timebetov.microblog.services.impl;

import com.github.timebetov.microblog.configs.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redis;

    public void addToBlacklist(String token, long expirationTime) {

        redis.opsForValue().set(token, AppConstants.JWT_BLACKLISTED, expirationTime, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return redis.hasKey(token);
    }
}
