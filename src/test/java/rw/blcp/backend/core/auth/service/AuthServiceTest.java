package rw.blcp.backend.core.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import rw.blcp.backend.common.RecordState;
import rw.blcp.backend.core.auth.dto.LoginRequest;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.auth.entity.UserSession;
import rw.blcp.backend.core.auth.repository.UserRepository;
import rw.blcp.backend.core.auth.repository.UserSessionRepository;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.fixtures.TestFixtures;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UserRepository userRepository;
  @Mock UserSessionRepository userSessionRepository;
  @Mock JwtService jwtService;
  @Mock PasswordEncoder passwordEncoder;
  @InjectMocks AuthService authService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(authService, "refreshExpirationHours", 8L);
  }

  @Test
  void login_savesSessionWithHashedToken_notTheRawValue() {
    User user = activeUser();
    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("pass", user.getPassword())).thenReturn(true);
    when(jwtService.generateAccessToken(user)).thenReturn("jwt");
    when(userSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    AuthService.LoginResult result = authService.login(new LoginRequest("user@test.com", "pass"));

    ArgumentCaptor<UserSession> captor = forClass(UserSession.class);
    verify(userSessionRepository).save(captor.capture());
    assertThat(captor.getValue().getTokenHash())
        .as("Token must be hashed before storage")
        .isNotEqualTo(result.rawRefreshToken());
  }

  @Test
  void login_whenUserNotFound_throwsInvalidCredentials() {
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@test.com", "pass")))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  void login_whenPasswordWrong_throwsInvalidCredentials() {
    User user = activeUser();
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(any(), any())).thenReturn(false);

    assertThatThrownBy(() -> authService.login(new LoginRequest("user@test.com", "wrong")))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

    verify(userSessionRepository, never()).save(any());
  }

  @Test
  void login_whenAccountInactive_throwsInvalidCredentials() {
    User user = activeUser();
    user.setState(RecordState.INACTIVE);
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> authService.login(new LoginRequest("user@test.com", "pass")))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

    verify(userSessionRepository, never()).save(any());
  }

  @Test
  void refresh_marksOldSessionUsedAndCreatesRotatedSession() {
    User user = activeUser();
    UserSession stored = sessionFor(user, false, Instant.now().plus(1, ChronoUnit.HOURS));
    when(userSessionRepository.findByTokenHash(any())).thenReturn(Optional.of(stored));
    when(jwtService.generateAccessToken(user)).thenReturn("new-jwt");
    when(userSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    authService.refresh("raw-token");

    assertThat(stored.isUsed()).as("Old session must be marked used after rotation").isTrue();
    ArgumentCaptor<UserSession> captor = forClass(UserSession.class);
    verify(userSessionRepository, times(2)).save(captor.capture());
    assertThat(captor.getAllValues().get(1).isUsed())
        .as("Newly issued session must not be pre-used")
        .isFalse();
  }

  @Test
  void refresh_whenTokenAlreadyUsed_revokesAllSessionsAndThrows() {
    User user = activeUser();
    UserSession reused = sessionFor(user, true, Instant.now().plus(1, ChronoUnit.HOURS));
    when(userSessionRepository.findByTokenHash(any())).thenReturn(Optional.of(reused));

    assertThatThrownBy(() -> authService.refresh("reused-token"))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.TOKEN_INVALID);

    verify(userSessionRepository).deleteAllByUser(user);
  }

  @Test
  void refresh_whenSessionExpired_throwsTokenExpired() {
    User user = activeUser();
    UserSession expired = sessionFor(user, false, Instant.now().minus(1, ChronoUnit.HOURS));
    when(userSessionRepository.findByTokenHash(any())).thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> authService.refresh("expired-token"))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.TOKEN_EXPIRED);
  }

  @Test
  void logout_deletesAllSessionsForTheUser() {
    User user = activeUser();
    UserSession session = sessionFor(user, false, Instant.now().plus(1, ChronoUnit.HOURS));
    when(userSessionRepository.findByTokenHash(any())).thenReturn(Optional.of(session));

    authService.logout("raw-token");

    verify(userSessionRepository).deleteAllByUser(user);
  }

  @Test
  void logout_withUnknownToken_doesNothing() {
    when(userSessionRepository.findByTokenHash(any())).thenReturn(Optional.empty());

    authService.logout("unknown-token");

    verify(userSessionRepository, never()).deleteAllByUser(any());
  }

  private User activeUser() {
    User user = TestFixtures.userWithRoles();
    user.setEmail("user@test.com");
    user.setPassword("hashed-pass");
    return user;
  }

  private UserSession sessionFor(User user, boolean used, Instant expiresAt) {
    UserSession session = new UserSession();
    session.setUser(user);
    session.setTokenHash("some-hash");
    session.setUsed(used);
    session.setExpiresAt(expiresAt);
    return session;
  }
}
