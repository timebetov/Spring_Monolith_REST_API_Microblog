package com.github.timebetov.microblog.repository;

import com.github.timebetov.microblog.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}