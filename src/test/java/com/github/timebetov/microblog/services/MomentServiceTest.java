//package com.github.timebetov.microblog.service;
//
//import com.github.timebetov.microblog.dto.moment.MomentDTO;
//import com.github.timebetov.microblog.dto.moment.RequestMomentDTO;
//import com.github.timebetov.microblog.exception.ResourceNotFoundException;
//import com.github.timebetov.microblog.mapper.MomentMapper;
//import com.github.timebetov.microblog.model.Moment;
//import com.github.timebetov.microblog.model.User;
//import com.github.timebetov.microblog.repository.MomentDao;
//import com.github.timebetov.microblog.repository.UserDao;
//import com.github.timebetov.microblog.service.impl.MomentService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//public class MomentServiceTest {
//
//    @MockitoBean
//    private MomentDao momentDao;
//
//    @MockitoBean
//    private UserDao userDao;
//
//    @Autowired
//    private MomentService momentService;
//
//    MomentDTO momentDTO;
//    Moment moment;
//    User author;
//
//    @BeforeEach
//    void setUp() {
//
//        this.author = User.builder()
//                .userId(1L)
//                .username("richard")
//                .email("richard@gmail.com")
//                .role(User.Role.USER)
//                .build();
//
//        this.moment = Moment.builder()
//                .momentId(UUID.randomUUID())
//                .text("Hello World this is a test text")
//                .author(author)
//                .visibility(Moment.Visibility.PUBLIC)
//                .build();
//
//        this.momentDTO = MomentDTO.builder()
//                .text("Hello World this is a test text")
//                .authorId(1L)
//                .visibility("PUBLIC")
//                .build();
//    }
//
//    @Test
//    @DisplayName("createMoment: Should create a new Moment")
//    public void createNewMomentTest() {
//
//        RequestMomentDTO reqMomentDTO = RequestMomentDTO.builder()
//                .text("Hello World this is a test text")
//                .visibility("PUBLIC")
//                .build();
//
//        when(userDao.findById(1L)).thenReturn(Optional.of(author));
//        when(momentDao.save(any(Moment.class))).thenReturn(moment);
//
//        momentService.createMoment(1L, reqMomentDTO);
//
//        ArgumentCaptor<Moment> momentCaptor = ArgumentCaptor.forClass(Moment.class);
//        verify(momentDao).save(momentCaptor.capture());
//        Moment createdMoment = momentCaptor.getValue();
//
//        assertNotNull(createdMoment, "Created moment should not be null");
//        assertEquals("Hello World this is a test text", createdMoment.getText());
//        assertEquals(1L, moment.getAuthor().getUserId());
//        assertEquals(Moment.Visibility.PUBLIC, moment.getVisibility());
//
//        verify(userDao, times(1)).findById(1L);
//        verify(momentDao, times(1)).save(any(Moment.class));
//    }
//
//    @Test
//    @DisplayName("createMomentInvalidAuthor: Should throw an ResourceNotFoundException")
//    public void createMomentInvalidAuthorTest() {
//
//        when(userDao.findById(2L)).thenReturn(Optional.empty());
//        assertThrows(ResourceNotFoundException.class, () -> momentService.createMoment(2L, new RequestMomentDTO()));
//        verify(userDao, times(1)).findById(2L);
//    }
//
//    @Test
//    @DisplayName("getMomentById: Should return a Moment by given UUID")
//    public void getMomentByIdTest() {
//
//        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.of(moment));
//
//        MomentDTO foundMoment = momentService.getMomentById(UUID.randomUUID());
//
//        assertNotNull(foundMoment, "Should return a moment");
//        assertEquals("Hello World this is a test text", foundMoment.getText());
//        assertEquals(1L, moment.getAuthor().getUserId());
//        assertEquals(Moment.Visibility.PUBLIC, moment.getVisibility());
//
//        verify(momentDao, times(1)).findById(any(UUID.class));
//    }
//
//    @Test
//    @DisplayName("getMomentInvalidUUID: Should throw an ResourceNotFoundException")
//    public void getMomentByIdInvalidUUIDTest() {
//
//        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
//        assertThrows(ResourceNotFoundException.class, () -> momentService.getMomentById(UUID.randomUUID()));
//        verify(momentDao, times(1)).findById(any(UUID.class));
//    }
//
//    @Test
//    @DisplayName("getMomentsByAuthor: Should return list of all moments by author id")
//    public void getMomentsByAuthorIdTest() {
//
//        when(momentDao.findMomentByAuthor_UserId(1L)).thenReturn(List.of(moment));
//
//        List<MomentDTO> foundMoments = momentService.getMoments(1L, Moment.Visibility.PUBLIC);
//
//        assertNotNull(foundMoments, "Should return a list of moments");
//        assertEquals(1, foundMoments.size(), "Should return 1 moment");
//        assertEquals("Hello World this is a test text", foundMoments.get(0).getText());
//
//        verify(momentDao, times(1)).findMomentByAuthor_UserId(1L);
//    }
//
//    @Test
//    @DisplayName("getMomentsByAuthorIdEmptyList: Should return empty list")
//    public void getMomentsByAuthorIdEmptyListTest() {
//
//        when(momentDao.findMomentByAuthor_UserId(1L)).thenReturn(List.of());
//        List<MomentDTO> foundMoments = momentService.getMoments(1L, Moment.Visibility.PUBLIC);
//
//        assertNotNull(foundMoments, "Should return a list of moments NOT NULL");
//        assertEquals(0, foundMoments.size(), "Should return 0 moments");
//        verify(momentDao, times(1)).findMomentByAuthor_UserId(1L);
//    }
//
//    @Test
//    @DisplayName("updateMoment: Should return updated MomentDTO")
//    public void updateMomentTest() {
//
//        RequestMomentDTO requestMomentDTO = RequestMomentDTO.builder()
//                .text("Hello World this is an updated test text".toUpperCase())
//                .visibility("DRAFT")
//                .build();
//
//        moment.setText("Hello World this is an updated test text".toUpperCase());
//        moment.setVisibility(Moment.Visibility.DRAFT);
//
//        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.of(moment));
//        when(momentDao.save(any(Moment.class))).thenReturn(moment);
//
//        momentService.updateMoment(UUID.randomUUID(), requestMomentDTO);
//        ArgumentCaptor<Moment> momentCaptor = ArgumentCaptor.forClass(Moment.class);
//        verify(momentDao).save(momentCaptor.capture());
//        MomentDTO updatedMoment = MomentMapper.mapToMomentDTO(momentCaptor.getValue());
//
//        assertNotNull(updatedMoment, "Updated moments should not be null");
//        assertEquals("Hello World this is an updated test text".toUpperCase(), updatedMoment.getText());
//        assertEquals("DRAFT", updatedMoment.getVisibility());
//
//        verify(momentDao, times(1)).findById(any(UUID.class));
//        verify(momentDao, times(1)).save(any(Moment.class));
//    }
//
//    @Test
//    @DisplayName("updateMomentInvalidUUID: Should throw an ResourceNotFoundException")
//    public void updateMomentByIdInvalidUUIDTest() {
//
//        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
//        assertThrows(ResourceNotFoundException.class, () -> momentService.updateMoment(UUID.randomUUID(), null));
//        verify(momentDao, times(1)).findById(any(UUID.class));
//    }
//
//    @Test
//    @DisplayName("deleteMoment: Should delete moment without Exception")
//    public void deleteMomentTest() {
//
//        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.of(moment));
//        momentService.deleteMoment(UUID.randomUUID());
//        verify(momentDao, times(1)).findById(any(UUID.class));
//        verify(momentDao, times(1)).delete(any(Moment.class));
//    }
//
//    @Test
//    @DisplayName("deleteMomentInvalidUUID: Should throw an ResourceNotFoundException")
//    public void deleteMomentByIdInvalidUUIDTest() {
//
//        when(momentDao.findById(any(UUID.class))).thenReturn(Optional.empty());
//        assertThrows(ResourceNotFoundException.class, () -> momentService.deleteMoment(UUID.randomUUID()));
//        verify(momentDao, times(1)).findById(any(UUID.class));
//    }
//}
