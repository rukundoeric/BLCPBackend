package rw.blcp.backend.core.officer.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.blcp.backend.common.dto.ApiResponse;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.auth.annotation.RequiredRoles;
import rw.blcp.backend.core.officer.record.AssignOfficerRoleRequest;
import rw.blcp.backend.core.officer.record.CreateOfficerRequest;
import rw.blcp.backend.core.officer.record.OfficerResponse;
import rw.blcp.backend.core.officer.service.OfficerService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@RequiredRoles({RoleName.ADMIN})
public class OfficerController {

  private final OfficerService officerService;

  @PostMapping("/officers")
  public ResponseEntity<ApiResponse<OfficerResponse>> createOfficer(
      @Valid @RequestBody CreateOfficerRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.of(officerService.createOfficer(request)));
  }

  @PostMapping("/users/{userId}/officer-role")
  public ResponseEntity<ApiResponse<OfficerResponse>> assignOfficerRole(
      @PathVariable UUID userId, @Valid @RequestBody AssignOfficerRoleRequest request) {
    return ResponseEntity.ok(ApiResponse.of(officerService.assignOfficerRole(userId, request)));
  }
}
