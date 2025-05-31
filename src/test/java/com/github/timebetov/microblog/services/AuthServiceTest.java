package com.github.timebetov.microblog.services;

import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.LoginUserDTO;
import com.github.timebetov.microblog.exceptions.AlreadyExistsException;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.models.UserDetailsImpl;
import com.github.timebetov.microblog.repository.UserDao;
import com.github.timebetov.microblog.services.impl.AuthService;
import com.github.timebetov.microblog.services.impl.TokenBlacklistService;
import com.github.timebetov.microblog.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklistService blacklistService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    User user;

    @BeforeEach
    void setup() {
        this.user = User.builder()
                .userId(1L)
                .username("alexkey")
                .email("alex@gmail.com")
                .password("Alexkey2@25pwd")
                .role(User.Role.USER)
                .build();
    }

    @Test
    @DisplayName("should save new user when username and email are unique")
    void shouldCreateAndSaveUserWhenUsernameAndEmailAreUnique() {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("user1")
                .email("user1@gmail.com")
                .password("User1pwd2@25")
                .confirmPassword("User1pwd2@25")
                .build();

        when(userDao.existsByUsername(createUserDTO.getUsername())).thenReturn(false);
        when(userDao.existsByEmail(createUserDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("User1pwd2@25")).thenReturn("$2a$12$5eAh0HIekuEJd8HrmvJFBeNSKnJMnv1IrWiSvob6OmH4ExOhD9UBO");

        assertDoesNotThrow(() -> authService.register(createUserDTO));

        verify(userDao, times(1)).save(any(User.class));
        verify(userDao, times(1)).existsByUsername(createUserDTO.getUsername());
        verify(userDao, times(1)).existsByEmail(createUserDTO.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("user1", savedUser.getUsername());
        assertEquals("user1@gmail.com", savedUser.getEmail());
        assertEquals(User.Role.USER, savedUser.getRole());
        assertNotNull(savedUser.getPassword(), "Password is null");
        assertTrue(savedUser.getPassword().startsWith("$2a$12"));
    }

    @Test
    @DisplayName("should throw an AlreadyExistsException when saving new user due to email has already taken")
    void shouldThrowEmailAlreadyExistsExceptionWhenSavingUser() {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("user1")
                .email("user1@gmail.com")
                .password("User1pwd2@25")
                .confirmPassword("User1pwd2@25")
                .build();

        when(userDao.existsByEmail(createUserDTO.getEmail())).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> authService.register(createUserDTO));
        verify(userDao, times(1)).existsByEmail(createUserDTO.getEmail());
    }

    @Test
    @DisplayName("should throw an AlreadyExistsException when saving new user due to username has already taken")
    void shouldThrowUsernameAlreadyExistsExceptionWhenSavingUser() {

        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .username("user1")
                .email("user1@gmail.com")
                .password("User1pwd2@25")
                .confirmPassword("User1pwd2@25")
                .build();

        when(userDao.existsByUsername(createUserDTO.getUsername())).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> authService.register(createUserDTO));
        verify(userDao, times(1)).existsByUsername(createUserDTO.getUsername());
    }

    @Test
    @DisplayName("should throw BadCredentialsException when logging in")
    void shouldThrowBadCredentialsExceptionWhenLoggingIn() {

        LoginUserDTO request = new LoginUserDTO("alexkey", "Alexkey2@25pwd");

        Authentication authentication = new TestingAuthenticationToken(request.getUsername(), request.getPassword());

        when(authenticationManager.authenticate(authentication)).thenReturn(authentication);
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(authenticationManager, times(1)).authenticate(authentication);
    }

    @Test
    @DisplayName("should return jwt token when logging in with valid credentials")
    void shouldReturnTokenWhenLoggingValidCredentials() {

        LoginUserDTO request = new LoginUserDTO("user1", "Alexkey2@25pwd");
        UserDetailsImpl currentUser = UserDetailsImpl.builder()
                .userId(2L)
                .username("user1")
                .email("user1@gmail.com")
                .password("Alexkey2@25pwd")
                .role("USER")
                .build();

        Authentication authentication = new TestingAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication authenticated = new TestingAuthenticationToken(currentUser, request.getPassword());
        authenticated.setAuthenticated(true);

        when(authenticationManager.authenticate(authentication)).thenReturn(authenticated);

        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMSIsInJvbGUiOiJVU0VSIiwidXNlcklkIjoyLCJlbWFpbCI6InVzZXIxQGdtYWlsLmNvbSIsImlhdCI6MTc0ODYwNDE3NSwiaXNzIjoiYXBwLmp3dC5pc3N1ZXIiLCJleHAiOjE3NDg2OTA1NzV9.M6tkUon56d6dbOS_BA22SdWneGhGfw-R5ss7UD_AT-y9uzmXbyv65aKZmt6YF0Ow1f0EbXHPJ5kkGLmJzlsrIg";
        when(jwtUtils.generateJwtToken(currentUser)).thenReturn(token);

        assertEquals(token, authService.login(request));
    }

    @Test
    @DisplayName("should add token to blacklist when logging out")
    void shouldAddToBlacklistWhenLoggingOut() {

        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMSIsInJvbGUiOiJVU0VSIiwidXNlcklkIjoyLCJlbWFpbCI6InVzZXIxQGdtYWlsLmNvbSIsImlhdCI6MTc0ODYwNDE3NSwiaXNzIjoiYXBwLmp3dC5pc3N1ZXIiLCJleHAiOjE3NDg2OTA1NzV9.M6tkUon56d6dbOS_BA22SdWneGhGfw-R5ss7UD_AT-y9uzmXbyv65aKZmt6YF0Ow1f0EbXHPJ5kkGLmJzlsrIg";
        String bearerToken = "Bearer " + token;

        Date expirationTime = new Date(System.currentTimeMillis() + 100_000);
        long expectedTtl = expirationTime.getTime() - System.currentTimeMillis();

        when(jwtUtils.extractExpiration(token)).thenReturn(expirationTime);
        assertDoesNotThrow(() -> authService.logout(bearerToken));

        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        verify(blacklistService).addToBlacklist(eq(token), ttlCaptor.capture());

        Long actualTtl = ttlCaptor.getValue();
        assertNotNull(actualTtl);
        assertTrue(actualTtl > 0 && actualTtl <= expectedTtl);
    }
}
