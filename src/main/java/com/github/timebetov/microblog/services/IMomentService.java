package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.moment.MomentDTO;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.validations.EnumValues;

import java.util.List;
import java.util.UUID;

public interface IMomentService {

    void createMoment(Long authorId, RequestMomentDTO momentDetails);
    List<MomentDTO> getMoments(Long authorId, @EnumValues(enumClass = Moment.Visibility.class) String visibility, CurrentUserContext currentUser);
    MomentDTO getMomentById(UUID momentUUId, CurrentUserContext currentUser);
    void updateMoment(UUID momentId, RequestMomentDTO momentDetails, CurrentUserContext currentUser);
    void deleteMoment(UUID momentId, CurrentUserContext currentUser);
}
