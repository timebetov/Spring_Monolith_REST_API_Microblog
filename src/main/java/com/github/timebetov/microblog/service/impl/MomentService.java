package com.github.timebetov.microblog.service.impl;

import com.github.timebetov.microblog.dto.moment.MomentDTO;
import com.github.timebetov.microblog.dto.moment.RequestMomentDTO;
import com.github.timebetov.microblog.exception.ResourceNotFoundException;
import com.github.timebetov.microblog.mapper.MomentMapper;
import com.github.timebetov.microblog.model.Moment;
import com.github.timebetov.microblog.model.User;
import com.github.timebetov.microblog.repository.MomentDao;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.service.IMomentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MomentService implements IMomentService {

    private final MomentDao momentDao;
    private final UserDao userDao;

    @Override
    public void createMoment(Long authorId, RequestMomentDTO momentDetails) {

        User author = userDao.findById(authorId).orElseThrow(
                () -> new ResourceNotFoundException("User", "userId", String.valueOf(authorId)));

        Moment momentToSave = MomentMapper.mapRequestMomentDTOToMoment(momentDetails, new Moment());
        momentToSave.setCreatedAt(LocalDateTime.now());
        momentToSave.setCreatedBy(author.getUsername());
        momentToSave.setAuthor(author);

        Moment savedMoment = momentDao.save(momentToSave);
        MomentMapper.mapToMomentDTO(savedMoment);
    }

    @Override
    public MomentDTO getMomentById(UUID momentUUId) {

        Moment foundMoment = momentDao.findById(momentUUId).orElseThrow(
                () -> new ResourceNotFoundException("Moment", "momentUUID", momentUUId.toString()));

        return MomentMapper.mapToMomentDTO(foundMoment);
    }

    @Override
    public List<MomentDTO> getByAuthorId(Long authorId) {

        List<MomentDTO> moments = new ArrayList<>();
        momentDao.findMomentByAuthor_UserId(authorId)
                .forEach(m -> moments.add(MomentMapper.mapToMomentDTO(m)));
        return moments;
    }

    @Override
    public void updateMoment(UUID momentId, RequestMomentDTO momentDetails) {

        Moment foundMoment = momentDao.findById(momentId).orElseThrow(
                () -> new ResourceNotFoundException("Moment", "id", momentId.toString())
        );

        foundMoment.setVisibility(Moment.Visibility.valueOf(momentDetails.getVisibility()));
        foundMoment.setText(momentDetails.getText());
        Moment savedMoment = momentDao.save(foundMoment);
        MomentMapper.mapToMomentDTO(savedMoment);
    }

    @Override
    public void deleteMoment(UUID momentId) {

        Moment foundMoment = momentDao.findById(momentId).orElseThrow(
                () -> new ResourceNotFoundException("Moment", "id", momentId.toString())
        );
        momentDao.delete(foundMoment);
    }
}
