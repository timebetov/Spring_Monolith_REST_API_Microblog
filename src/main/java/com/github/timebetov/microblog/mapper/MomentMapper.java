package com.github.timebetov.microblog.mapper;

import com.github.timebetov.microblog.dto.MomentDTO;
import com.github.timebetov.microblog.model.Moment;

public class MomentMapper {

    public static MomentDTO mapToMomentDTO(Moment moment, MomentDTO momentDTO) {

        momentDTO.setText(moment.getText());
        momentDTO.setAuthorId(moment.getAuthor().getUserId());
        momentDTO.setVisibility(moment.getVisibility().name());
        return momentDTO;
    }

    public static Moment mapToMoment(MomentDTO momentDTO, Moment moment) {

        moment.setText(momentDTO.getText());
        moment.setVisibility(Moment.Visibility.valueOf(momentDTO.getVisibility()));
        return moment;
    }
}
