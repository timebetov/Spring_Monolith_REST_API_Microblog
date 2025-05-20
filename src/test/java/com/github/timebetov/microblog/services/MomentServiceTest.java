package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.moment.MomentDTO;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import com.github.timebetov.microblog.mappers.MomentMapper;
import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.repository.MomentDao;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.impl.FollowService;
import com.github.timebetov.microblog.services.impl.MomentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class MomentServiceTest {

    @MockitoBean
    private MomentDao momentDao;

    @MockitoBean
    private UserDao userDao;

    @MockitoBean
    private FollowService followService;

    @Autowired
    private MomentService momentService;

    Moment publicMoment1;
    Moment publicMoment2;

    Moment draft1;
    Moment draft2;

    Moment forFollowers1;
    Moment forFollowers2;

    User author;

    CurrentUserContext otherUser;
    CurrentUserContext admin;

    @BeforeEach
    void setUp() {

        this.author = User.builder()
                .userId(1L)
                .username("richard")
                .email("richard@gmail.com")
                .role(User.Role.USER)
                .build();

        this.otherUser = CurrentUserContext.builder()
                .userId(2L)
                .role(User.Role.USER)
                .build();

        this.admin = CurrentUserContext.builder()
                .userId(3L)
                .role(User.Role.ADMIN)
                .build();

        this.publicMoment1 = Moment.builder()
                .momentId(UUID.randomUUID())
                .text("FOR ALL USERS")
                .author(author)
                .visibility(Moment.Visibility.PUBLIC)
                .build();

        this.publicMoment2 = Moment.builder()
                .momentId(UUID.randomUUID())
                .text("FOR ALL USERS")
                .author(author)
                .visibility(Moment.Visibility.PUBLIC)
                .build();

        this.draft1 = Moment.builder()
                .momentId(UUID.randomUUID())
                .author(author)
                .visibility(Moment.Visibility.DRAFT)
                .text("DRAFT 1")
                .build();

        this.draft2 = Moment.builder()
                .momentId(UUID.randomUUID())
                .author(author)
                .visibility(Moment.Visibility.DRAFT)
                .text("DRAFT 2")
                .build();

        this.forFollowers1 = Moment.builder()
                .momentId(UUID.randomUUID())
                .author(author)
                .visibility(Moment.Visibility.FOLLOWERS_ONLY)
                .text("FOR ONLY FOLLOWERS")
                .build();

        this.forFollowers2 = Moment.builder()
                .momentId(UUID.randomUUID())
                .author(author)
                .visibility(Moment.Visibility.FOLLOWERS_ONLY)
                .text("FOR ONLY FOLLOWERS")
                .build();
    }

    /**
     * <h4>Tests saving a valid moment.</h4>
     *
     * <p><b>Expected:</b> Moment is persisted successfully.</p>
     */
    @Test
    @DisplayName("should save a moment")
    public void shouldSaveMoment() {

        RequestMomentDTO reqMomentDTO = RequestMomentDTO.builder()
                .text("FOR ALL USERS")
                .visibility("PUBLIC")
                .build();

        when(userDao.findById(1L)).thenReturn(Optional.of(author));
        when(momentDao.save(any(Moment.class))).thenReturn(publicMoment1);

        momentService.createMoment(1L, reqMomentDTO);

        ArgumentCaptor<Moment> momentCaptor = ArgumentCaptor.forClass(Moment.class);
        verify(momentDao).save(momentCaptor.capture());
        Moment createdMoment = momentCaptor.getValue();

        assertNotNull(createdMoment, "Created moment should not be null");
        assertEquals("FOR ALL USERS", createdMoment.getText());
        assertEquals(1L, publicMoment1.getAuthor().getUserId());
        assertEquals(Moment.Visibility.PUBLIC, publicMoment1.getVisibility());

        verify(userDao, times(1)).findById(1L);
        verify(momentDao, times(1)).save(any(Moment.class));
    }

    /**
     * <h4>Tests that saving a moment fails when the author ID is invalid.</h4>
     *
     * <p><b>Expected:</b> Throws {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException}.</p>
     *
     */
    @Test
    @DisplayName("should throw exception when saving moment with invalid authorId")
    public void shouldThrowExceptionWhenSavingMomentWithInvalidAuthorId() {

        when(userDao.findById(100L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.createMoment(100L, new RequestMomentDTO()));
        verify(userDao, times(1)).findById(100L);
    }

    /**
     * <h4>Tests retrieving a moment by its UUID</h4>
     *
     * <p><b>Expected:</b> Returns the corresponding moment if it exists.</p>
     */
    @Test
    @DisplayName("should return a moment by id")
    public void shouldReturnMomentById() {

        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.of(publicMoment1));

        MomentDTO foundMoment = momentService.getMomentById(UUID.randomUUID());

        assertNotNull(foundMoment, "Should return a moment");
        assertEquals("FOR ALL USERS", foundMoment.getText());
        assertEquals(1L, publicMoment1.getAuthor().getUserId());
        assertEquals(Moment.Visibility.PUBLIC, publicMoment1.getVisibility());

        verify(momentDao, times(1)).findById(any(UUID.class));
    }

    /**
     * <h4>Tests that retrieving a moment with an invalid UUID throws an exception</h4>
     *
     * <p><b>Expected:</b> {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException} is thrown.</p>
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when retrieving moment with invalid id")
    public void shouldThrowResourceNotFoundExceptionWhenRetrievingMomentWithInvalidId() {

        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.getMomentById(UUID.randomUUID()));
        verify(momentDao, times(1)).findById(any(UUID.class));
    }

    /**
     * <h4>Tests that a moment is updated and saved correctly.</h4>
     *
     * <ul>
     *     <li><b>Given:</b> An existing moment and a valid {@link RequestMomentDTO} update payload.</li>
     *     <li><b>When:</b> The {@code updateMoment} method is called.</li>
     *     <li><b>Then:</b> The moment should be updated with the new data and persisted.</li>
     * </ul>
     */
    @Test
    @DisplayName("should update and save moment")
    public void shouldUpdateMoment() {

        UUID momentId = UUID.randomUUID();

        RequestMomentDTO requestMomentDTO = RequestMomentDTO.builder()
                .text("Hello World this is an updated test text".toUpperCase())
                .visibility("DRAFT")
                .build();

        publicMoment1.setText("Hello World this is an updated test text".toUpperCase());
        publicMoment1.setVisibility(Moment.Visibility.DRAFT);

        when(momentDao.findById(momentId)).thenReturn(Optional.of(publicMoment1));
        when(momentDao.save(any(Moment.class))).thenReturn(publicMoment1);

        momentService.updateMoment(momentId, requestMomentDTO);

        ArgumentCaptor<Moment> momentCaptor = ArgumentCaptor.forClass(Moment.class);
        verify(momentDao).save(momentCaptor.capture());
        MomentDTO updatedMoment = MomentMapper.mapToMomentDTO(momentCaptor.getValue());

        assertNotNull(updatedMoment, "Updated moments should not be null");
        assertEquals("Hello World this is an updated test text".toUpperCase(), updatedMoment.getText());
        assertEquals("DRAFT", updatedMoment.getVisibility());

        verify(momentDao, times(1)).findById(any(UUID.class));
        verify(momentDao, times(1)).save(any(Moment.class));
    }

    /**
     * <h4>Tests updating a moment with invalid ID. Throws an Exception</h4>
     *
     * <p><b>Expected:</b> {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException} is thrown.</p>
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when updating moment with invalid id")
    public void shouldThrowResourceNotFoundExceptionWhenUpdatingMomentWithInvalidId() {

        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.updateMoment(UUID.randomUUID(), null));
        verify(momentDao, times(1)).findById(any(UUID.class));
    }

    /**
     * <h4>Tests that a moment is successfully deleted when a valid ID is provided.</h4>
     *
     * <p><b>Expected:</b> After deletion, attempting to retrieve the moment results in {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException}.</p>
     */
    @Test
    @DisplayName("should delete moment by id")
    public void shouldDeleteMomentById() {

        UUID momentID = UUID.randomUUID();

        when(momentDao.findById(any(UUID.class)))
                .thenReturn(Optional.of(publicMoment1))
                .thenReturn(Optional.empty());

        momentService.deleteMoment(momentID);
        assertThrows(ResourceNotFoundException.class, () ->momentService.getMomentById(momentID));

        verify(momentDao, times(2)).findById(any(UUID.class));
        verify(momentDao, times(1)).delete(any(Moment.class));
    }

    /**
     * <h4>Tests deleting a moment wih invalid UUID. Throws an Exception</h4>
     *
     * <p><b>Expected:</b> {@link com.github.timebetov.microblog.exceptions.ResourceNotFoundException} is thrown.</p>
     */
    @Test
    @DisplayName("should throw ResourceNotFoundException when deleting moment with invalid id")
    public void shouldThrowResourceNotFoundExceptionWhenDeletingMomentWithInvalidId() {

        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> momentService.deleteMoment(UUID.randomUUID()));
        verify(momentDao, times(1)).findById(any(UUID.class));
    }
}
