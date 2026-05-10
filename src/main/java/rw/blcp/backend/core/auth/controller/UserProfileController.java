package rw.blcp.backend.core.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.blcp.backend.common.dto.ApiResponse;
import rw.blcp.backend.core.auth.dto.UserProfileResponse;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.auth.service.UserProfileService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserProfileService userProfileService;

  @GetMapping("/user-profile")
  public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(ApiResponse.of(userProfileService.getProfile(currentUser)));
  }
}
