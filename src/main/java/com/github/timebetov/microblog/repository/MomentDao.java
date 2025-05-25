package com.github.timebetov.microblog.repository;

import com.github.timebetov.microblog.models.Moment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MomentDao extends CrudRepository<Moment, UUID> {

    List<Moment> findMomentByAuthor_UserId(Long authorUserId);

    @Query("SELECT m.author.userId FROM Moment m WHERE m.momentId = :momentId")
    Optional<Long> findAuthorIdByMomentId(@Param("momentId") UUID momentId);
}