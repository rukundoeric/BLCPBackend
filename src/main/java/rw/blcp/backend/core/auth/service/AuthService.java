package rw.blcp.backend.core.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.blcp.backend.common.RecordState;
import rw.blcp.backend.core.auth.dto.LoginRequest;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.auth.entity.UserSession;
import rw.blcp.backend.core.auth.repository.UserRepository;
import rw.blcp.backend.core.auth.repository.UserSessionRepository;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final UserSessionRepository userSessionRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.jwt.refresh-expiration-hours}")
  private long refreshExpirationHours;

  public record LoginResult(String accessToken, String rawRefreshToken) {}

  public record TokenRefreshResult(String accessToken, String rawRefreshToken) {}

  @Transactional
  public LoginResult login(LoginRequest request) {
    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));

    if (user.getState() != RecordState.ACTIVE) {
      log.warn("Login attempt for inactive account: {}", request.email());
      throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
    }

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      log.warn("Failed login attempt for: {}", request.email());
      throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
    }

    String accessToken = jwtService.generateAccessToken(user);
    String rawRefreshToken = UUID.randomUUID().toString();
    Instant expiresAt = Instant.now().plus(refreshExpirationHours, ChronoUnit.HOURS);

    UserSession session = new UserSession();
    session.setUser(user);
    session.setTokenHash(hash(rawRefreshToken));
    session.setExpiresAt(expiresAt);
    userSessionRepository.save(session);

    log.info("User {} logged in", user.getEmail());
    return new LoginResult(accessToken, rawRefreshToken);
  }

  @Transactional
  public TokenRefreshResult refresh(String rawRefreshToken) {
    String tokenHash = hash(rawRefreshToken);

    UserSession stored =
        userSessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID));

    if (stored.isUsed()) {
      log.warn(
          "Session token reuse detected for user {}. Revoking all sessions.",
          stored.getUser().getEmail());
      userSessionRepository.deleteAllByUser(stored.getUser());
      throw new ApiException(ErrorCode.TOKEN_INVALID);
    }

    if (stored.getExpiresAt().isBefore(Instant.now())) {
      log.warn("Expired session presented for user {}", stored.getUser().getEmail());
      throw new ApiException(ErrorCode.TOKEN_EXPIRED);
    }

    stored.setUsed(true);
    userSessionRepository.save(stored);

    User user = stored.getUser();
    String newAccessToken = jwtService.generateAccessToken(user);
    String newRawRefreshToken = UUID.randomUUID().toString();

    UserSession rotated = new UserSession();
    rotated.setUser(user);
    rotated.setTokenHash(hash(newRawRefreshToken));
    rotated.setExpiresAt(stored.getExpiresAt());
    userSessionRepository.save(rotated);

    log.info("Token rotated for user {}", user.getEmail());
    return new TokenRefreshResult(newAccessToken, newRawRefreshToken);
  }

  @Transactional
  public void logout(String rawRefreshToken) {
    userSessionRepository
        .findByTokenHash(hash(rawRefreshToken))
        .ifPresent(
            session -> {
              log.info("User {} logged out", session.getUser().getEmail());
              userSessionRepository.deleteAllByUser(session.getUser());
            });
  }

  @Transactional
  public void logoutByUser(User user) {
    log.info("User {} logged out (via access token)", user.getEmail());
    userSessionRepository.deleteAllByUser(user);
  }

  private String hash(String raw) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
