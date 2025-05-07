package com.github.timebetov.microblog.service;

import com.github.timebetov.microblog.dto.MomentDTO;
import com.github.timebetov.microblog.exception.ResourceNotFoundException;
import com.github.timebetov.microblog.mapper.MomentMapper;
import com.github.timebetov.microblog.model.Moment;
import com.github.timebetov.microblog.repository.MomentDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MomentService {

    private final MomentDao momentDao;

    public MomentDTO createMoment(MomentDTO momentDTO) {

        Moment savedMoment = momentDao.save(MomentMapper.mapToMoment(momentDTO, new Moment()));
        return MomentMapper.mapToMomentDTO(savedMoment, momentDTO);
    }

    public MomentDTO getMomentById(UUID momentUUId) {

        Moment foundMoment = momentDao.findById(momentUUId).orElseThrow(
                () -> new ResourceNotFoundException("Moment", "momentUUID", momentUUId.toString()));

        return MomentMapper.mapToMomentDTO(foundMoment, new MomentDTO());
    }

    public List<MomentDTO> getByAuthorId(long authorId) {

        List<MomentDTO> moments = new ArrayList<>();
        momentDao.findMomentByAuthor_UserId(authorId)
                .forEach(m -> moments.add(MomentMapper.mapToMomentDTO(m, new MomentDTO())));
        return moments;
    }

    public MomentDTO updateMoment(UUID momentId, MomentDTO updateMoment) {

        Moment foundMoment = momentDao.findById(momentId).orElseThrow(
                () -> new ResourceNotFoundException("Moment", "id", momentId.toString())
        );

        if (updateMoment.getVisibility() != null) {
            foundMoment.setVisibility(Moment.Visibility.valueOf(updateMoment.getVisibility()));
        }
        if (updateMoment.getText() != null) {
            foundMoment.setText(updateMoment.getText());
        }

        Moment savedMoment = momentDao.save(foundMoment);
        return MomentMapper.mapToMomentDTO(savedMoment, updateMoment);
    }

    public void deleteMoment(UUID momentId) {

        Moment foundMoment = momentDao.findById(momentId).orElseThrow(
                () -> new ResourceNotFoundException("Moment", "id", momentId.toString())
        );
        momentDao.delete(foundMoment);
    }
}
