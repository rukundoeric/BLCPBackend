package rw.blcp.backend.core.application.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rw.blcp.backend.common.dto.ApiResponse;
import rw.blcp.backend.core.application.record.ApplicationFilter;
import rw.blcp.backend.core.application.record.ApplicationResponse;
import rw.blcp.backend.core.application.record.CreateApplicationRequest;
import rw.blcp.backend.core.application.record.TakeActionRequest;
import rw.blcp.backend.core.application.service.ApplicationService;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.auth.annotation.RequiredRoles;
import rw.blcp.backend.core.auth.entity.User;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

  private final ApplicationService applicationService;

  @PostMapping(value = "/api/v1/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @RequiredRoles(RoleName.APPLICANT)
  public ResponseEntity<ApiResponse<ApplicationResponse>> create(
      @RequestPart("data") @Valid CreateApplicationRequest request,
      @RequestPart(value = "files", required = false) List<MultipartFile> files,
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.of(applicationService.create(request, files, currentUser)));
  }

  @PostMapping(
      value = "/api/v1/applications/{applicationNumber}/resubmit",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @RequiredRoles(RoleName.APPLICANT)
  public ResponseEntity<ApiResponse<ApplicationResponse>> resubmit(
      @PathVariable String applicationNumber,
      @RequestPart(value = "files", required = false) List<MultipartFile> files,
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(
        ApiResponse.of(applicationService.resubmit(applicationNumber, files, currentUser)));
  }

  @GetMapping("/api/v1/applications")
  @RequiredRoles({RoleName.OFFICER, RoleName.SENIOR_OFFICER, RoleName.APPLICANT})
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
