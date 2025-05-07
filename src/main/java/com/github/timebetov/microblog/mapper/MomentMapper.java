package com.github.timebetov.microblog.mapper;

import com.github.timebetov.microblog.dto.moment.MomentDTO;
import com.github.timebetov.microblog.dto.moment.RequestMomentDTO;
import com.github.timebetov.microblog.dto.user.UserDTO;
import com.github.timebetov.microblog.model.Moment;

public class MomentMapper {

    public static MomentDTO mapToMomentDTO(Moment moment) {

        MomentDTO result = new MomentDTO();
        result.setId(moment.getMomentId().toString());
        result.setText(moment.getText());
        result.setAuthorId(moment.getAuthor().getUserId());
        result.setVisibility(moment.getVisibility().name());
        return result;
    }

    public static Moment mapRequestMomentDTOToMoment(RequestMomentDTO requestMomentDTO, Moment moment) {

        moment.setText(requestMomentDTO.getText());
        moment.setVisibility(Moment.Visibility.valueOf(requestMomentDTO.getVisibility()));
        return moment;
    }
}
