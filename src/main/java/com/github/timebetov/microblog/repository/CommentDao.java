package com.github.timebetov.microblog.repository;

import com.github.timebetov.microblog.model.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentDao extends CrudRepository<Comment, UUID> {}
