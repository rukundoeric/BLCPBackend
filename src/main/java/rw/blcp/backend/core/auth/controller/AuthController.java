package rw.blcp.backend.core.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.blcp.backend.common.dto.ApiResponse;
import rw.blcp.backend.core.auth.dto.LoginRequest;
import rw.blcp.backend.core.auth.dto.LoginResponse;
import rw.blcp.backend.core.auth.service.AuthService;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;

@Slf4j
@RestController
@RequestMapping("/api/v1/public/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Value("${app.cookie.secure:true}")
  private boolean cookieSecure;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {

    AuthService.LoginResult result = authService.login(request);

    setRefreshTokenCookie(response, result.rawRefreshToken());

    return ResponseEntity.ok(ApiResponse.of(new LoginResponse(result.accessToken())));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<LoginResponse>> refresh(
      @CookieValue(name = "refreshToken", required = false) String rawRefreshToken,
      HttpServletResponse response) {

    if (rawRefreshToken == null) {
      log.warn("Token refresh attempted with no refresh cookie");
      throw new ApiException(ErrorCode.TOKEN_INVALID);
    }

    AuthService.TokenRefreshResult result = authService.refresh(rawRefreshToken);

    setRefreshTokenCookie(response, result.rawRefreshToken());

    return ResponseEntity.ok(ApiResponse.of(new LoginResponse(result.accessToken())));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @CookieValue(name = "refreshToken", required = false) String rawRefreshToken,
      HttpServletResponse response) {

    if (rawRefreshToken != null) {
      authService.logout(rawRefreshToken);
    }

    clearRefreshTokenCookie(response);

    return ResponseEntity.ok(ApiResponse.of(null));
  }

  private void setRefreshTokenCookie(HttpServletResponse response, String token) {
    ResponseCookie cookie =
        ResponseCookie.from("refreshToken", token)
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite("Strict")
            .path("/api/v1/public/auth/refresh")
            .maxAge(Duration.ofHours(8))
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private void clearRefreshTokenCookie(HttpServletResponse response) {
    ResponseCookie cookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite("Strict")
            .path("/api/v1/public/auth/refresh")
            .maxAge(Duration.ZERO)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
