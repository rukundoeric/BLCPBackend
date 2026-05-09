package rw.blcp.backend.workflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import rw.blcp.backend.auth.RoleName;
import rw.blcp.backend.auth.annotation.RequiredRoles;
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.core.dto.ApiResponse;
import rw.blcp.backend.workflow.dto.ApplicationFilter;
import rw.blcp.backend.workflow.dto.ApplicationResponse;
import rw.blcp.backend.workflow.dto.CreateApplicationRequest;
import rw.blcp.backend.workflow.dto.TakeActionRequest;
import rw.blcp.backend.workflow.service.ApplicationService;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

  private final ApplicationService applicationService;

  @PostMapping("/api/v1/public/applications")
  public ResponseEntity<ApiResponse<ApplicationResponse>> create(
      @Valid @RequestBody CreateApplicationRequest request,
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.of(applicationService.create(request, currentUser)));
  }

  @GetMapping("/api/v1/applications")
  @RequiredRoles({RoleName.OFFICER, RoleName.SENIOR_OFFICER})
  public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> fetchApplications(
      @ModelAttribute ApplicationFilter filter,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable,
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(
        ApiResponse.of(applicationService.fetchApplications(filter, pageable, currentUser)));
  }

  @PostMapping("/api/v1/applications/{applicationNumber}/action")
  @RequiredRoles({RoleName.OFFICER, RoleName.SENIOR_OFFICER})
  public ResponseEntity<ApiResponse<ApplicationResponse>> takeAction(
      @PathVariable String applicationNumber,
      @Valid @RequestBody TakeActionRequest request,
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(
        ApiResponse.of(applicationService.takeAction(applicationNumber, request, currentUser)));
  }
}
