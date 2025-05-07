package com.github.timebetov.microblog.repository;

import com.github.timebetov.microblog.model.Moment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MomentDao extends CrudRepository<Moment, UUID> {

    List<Moment> findMomentByAuthor_UserId(Long authorUserId);
}