package rw.blcp.backend.core.auth.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rw.blcp.backend.common.handler.GlobalExceptionHandler;
import rw.blcp.backend.core.auth.dto.LoginRequest;
import rw.blcp.backend.core.auth.service.AuthService;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;

@WebMvcTest(
    value = AuthController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean AuthService authService;

  @Test
  void login_withValidCredentials_returns200WithAccessTokenAndSetsRefreshCookie() throws Exception {
    when(authService.login(any()))
        .thenReturn(new AuthService.LoginResult("access-jwt", "raw-refresh-token"));

    mockMvc
        .perform(
            post("/api/v1/public/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new LoginRequest("user@test.com", "pass"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").value("access-jwt"))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=")));
  }

  @Test
  void login_withBlankPassword_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/public/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("user@test.com", ""))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.errorCode").value("VALIDATION_FAILED"));
  }

  @Test
  void login_withInvalidEmailFormat_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/public/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("not-an-email", "pass"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_whenCredentialsWrong_returns401() throws Exception {
    when(authService.login(any())).thenThrow(new ApiException(ErrorCode.INVALID_CREDENTIALS));

    mockMvc
        .perform(
            post("/api/v1/public/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new LoginRequest("user@test.com", "wrong"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.errorCode").value("INVALID_CREDENTIALS"));
  }

  @Test
  void refresh_withValidCookie_returns200AndRotatesRefreshCookie() throws Exception {
    when(authService.refresh("raw-refresh-token"))
        .thenReturn(new AuthService.TokenRefreshResult("new-access-jwt", "new-raw-refresh-token"));

    mockMvc
        .perform(
            post("/api/v1/public/auth/refresh")
                .cookie(new Cookie("refreshToken", "raw-refresh-token")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").value("new-access-jwt"))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=")));
  }

  @Test
  void refresh_withNoCookie_returns401() throws Exception {
    mockMvc
        .perform(post("/api/v1/public/auth/refresh"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.errorCode").value("TOKEN_INVALID"));
  }

  @Test
  void refresh_whenTokenHasAlreadyBeenUsed_returns401() throws Exception {
    when(authService.refresh(any())).thenThrow(new ApiException(ErrorCode.TOKEN_INVALID));

    mockMvc
        .perform(
            post("/api/v1/public/auth/refresh").cookie(new Cookie("refreshToken", "reused-token")))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.errorCode").value("TOKEN_INVALID"));
  }

  @Test
  void refresh_whenTokenExpired_returns401() throws Exception {
    when(authService.refresh(any())).thenThrow(new ApiException(ErrorCode.TOKEN_EXPIRED));

    mockMvc
        .perform(
            post("/api/v1/public/auth/refresh").cookie(new Cookie("refreshToken", "expired-token")))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.errorCode").value("TOKEN_EXPIRED"));
  }

  @Test
  void logout_withRefreshCookie_returns200AndClearsTheCookie() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/public/auth/logout")
                .cookie(new Cookie("refreshToken", "raw-refresh-token")))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

    verify(authService).logout("raw-refresh-token");
  }

  @Test
  void logout_withNoCookie_returns200WithoutCallingService() throws Exception {
    mockMvc
        .perform(post("/api/v1/public/auth/logout"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

    verify(authService, never()).logout(any());
  }
}
