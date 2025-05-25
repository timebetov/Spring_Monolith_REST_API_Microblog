package com.github.timebetov.microblog.mappers;

import com.github.timebetov.microblog.dtos.moment.MomentDTO;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.models.Moment;

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

        moment.setText(requestMomentDTO.getText().trim());
        if (requestMomentDTO.getVisibility() != null) {
            moment.setVisibility(Moment.Visibility.valueOf(requestMomentDTO.getVisibility().trim().toUpperCase()));
        }
        return moment;
    }
}
