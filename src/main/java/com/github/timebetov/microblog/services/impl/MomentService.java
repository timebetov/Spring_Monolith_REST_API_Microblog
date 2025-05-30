package com.github.timebetov.microblog.services.impl;

import com.github.timebetov.microblog.dtos.moment.MomentDTO;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import com.github.timebetov.microblog.mappers.MomentMapper;
import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.repository.MomentDao;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.IFollowService;
import com.github.timebetov.microblog.services.IMomentService;
import com.github.timebetov.microblog.validations.OnlyOwnerOrAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MomentService implements IMomentService {

    private final MomentDao momentDao;
    private final UserDao userDao;
    private final IFollowService followService;

    @Override
    public void createMoment(Long authorId, RequestMomentDTO momentDetails) {

        User author = userDao.findById(authorId).orElseThrow(
                () -> new ResourceNotFoundException("User", String.valueOf(authorId)));

        Moment momentToSave = MomentMapper.mapRequestMomentDTOToMoment(momentDetails, new Moment());

        // Visibility type by default will be `PUBLIC` if not defined
        if (momentToSave.getVisibility() == null)
            momentToSave.setVisibility(Moment.Visibility.PUBLIC);

        momentToSave.setAuthor(author);
        momentDao.save(momentToSave);
    }

    /**
     *
     * Requirements:
     * <ul>
     *     <li>Only admins and author himself can get moments with Visibility type `DRAFT`.</li>
     *     <li>Only admins and followers can get moments with Visibility type `FOLLOWERS_ONLY`.</li>
     *     <li>All users can get moments with Visibility type `PUBLIC`</li>
     * </ul>
     *
     * - So if authorId is null or empty, by default it will return all moments from database.
     * - Visibility type by default is `PUBLIC`.
     *
     * @param authorId can be null
     * @param visibility values `PUBLIC`, `FOLLOWERS_ONLY`, `DRAFT`
     *
     * @return List of moments based on given parameters.
     */

    @Override
    public List<MomentDTO> getMoments(Long authorId, String visibility, CurrentUserContext currentUser) {

        if (currentUser == null || currentUser.getUserId() == null)
            throw new IllegalArgumentException("User is not logged in");

        Moment.Visibility type = (visibility != null)
                ? Moment.Visibility.valueOf(visibility.trim().toUpperCase())
                : Moment.Visibility.PUBLIC;

        List<Moment> moments = (authorId != null)
                ? momentDao.findMomentByAuthor_UserId(authorId)
                : (List<Moment>) momentDao.findAll();

        if (authorId != null) {
            if (userDao.findById(authorId).isEmpty())
                throw new ResourceNotFoundException("User", String.valueOf(authorId));
            boolean isFollower = followService.isFollowing(currentUser.getUserId(), authorId);
            return moments.stream()
                    .filter(moment -> moment.getVisibility() == type)
                    .filter(moment -> moment.getVisibility().canBeViewedBy(currentUser, authorId, isFollower))
                    .map(MomentMapper::mapToMomentDTO)
                    .toList();
        } else {
            return moments.stream()
                    .filter(moment -> moment.getVisibility() == type)
                    .filter(moment -> {
                        Long momentAuthorId = moment.getAuthor().getUserId();
                        boolean isFollower = followService.isFollowing(currentUser.getUserId(), momentAuthorId);
                        return moment.getVisibility().canBeViewedBy(currentUser, momentAuthorId, isFollower);
                    })
                    .map(MomentMapper::mapToMomentDTO)
                    .toList();
        }
    }

    @Override
    public MomentDTO getMomentById(UUID momentUUId, CurrentUserContext currentUser) {

        if (currentUser == null || currentUser.getUserId() == null)
            throw new IllegalArgumentException("User is not logged in");

        Moment foundMoment = momentDao.findById(momentUUId).orElseThrow(
                () -> new ResourceNotFoundException("Moment", momentUUId.toString()));

        Long momentAuthorId = foundMoment.getAuthor().getUserId();
        boolean isFollower = followService.isFollowing(currentUser.getUserId(), momentAuthorId);

        if (!foundMoment.getVisibility().canBeViewedBy(currentUser, momentAuthorId, isFollower))
            throw new AccessDeniedException("You don't have permission to view this moment");

        return MomentMapper.mapToMomentDTO(foundMoment);
    }

    @Override
    @OnlyOwnerOrAdmin(ownerIdParam = "authorId")
    public void updateMoment(UUID momentId, RequestMomentDTO momentDetails, Long authorId) {

        Moment foundMoment = momentDao.findById(momentId).orElseThrow(
                () -> new ResourceNotFoundException("Moment", String.valueOf(momentId)));

        if (momentDetails.getVisibility() != null)
            foundMoment.setVisibility(Moment.Visibility.valueOf(momentDetails.getVisibility().toUpperCase()));

        foundMoment.setText(momentDetails.getText());
        momentDao.save(foundMoment);
    }

    @Override
    @OnlyOwnerOrAdmin(ownerIdParam = "authorId")
    public void deleteMoment(UUID momentId, Long authorId) {

        momentDao.deleteById(momentId);
    }

    @Override
    public Long getAuthorId(UUID momentUUId) {

        return momentDao.findAuthorIdByMomentId(momentUUId).orElseThrow(
                () -> new ResourceNotFoundException("Moment", momentUUId.toString()));
    }
}
