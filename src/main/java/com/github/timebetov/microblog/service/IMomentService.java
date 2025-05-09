package com.github.timebetov.microblog.service;

import com.github.timebetov.microblog.dto.moment.MomentDTO;
import com.github.timebetov.microblog.dto.moment.RequestMomentDTO;

import java.util.List;
import java.util.UUID;

public interface IMomentService {

    MomentDTO createMoment(Long authorId, RequestMomentDTO momentDetails);
    MomentDTO getMomentById(UUID momentUUId);
    List<MomentDTO> getByAuthorId(Long authorId);
    MomentDTO updateMoment(UUID momentId, RequestMomentDTO momentDetails);
    void deleteMoment(UUID momentId);
}
