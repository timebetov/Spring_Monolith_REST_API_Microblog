package com.github.timebetov.microblog.repository;

import com.github.timebetov.microblog.models.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("""
        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
        FROM User u
        JOIN u.follows f
        WHERE u.userId = :followerId AND f.userId = :followedId
    """)
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_follows (follower_id, followed_id) VALUES (:followerId, :followedId)", nativeQuery = true)
    void insertFollow(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_follows WHERE follower_id = :followerId AND followed_id = :followedId", nativeQuery = true)
    void deleteFollow(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    @Query("SELECT f FROM User u JOIN u.follows f WHERE u.userId = :userId")
    List<User> findFollowings(@Param("userId") Long userId);

    @Query("SELECT u FROM User u JOIN u.follows f WHERE f.userId = :userId")
    List<User> findFollowers(@Param("userId") Long userId);
}