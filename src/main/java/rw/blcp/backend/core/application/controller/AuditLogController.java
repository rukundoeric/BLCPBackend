package rw.blcp.backend.core.application.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rw.blcp.backend.common.dto.ApiResponse;
import rw.blcp.backend.core.application.record.AuditLogResponse;
import rw.blcp.backend.core.application.service.AuditLogService;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.auth.annotation.RequiredRoles;

@RestController
@RequiredArgsConstructor
public class AuditLogController {

  private final AuditLogService auditLogService;

  @GetMapping("/api/v1/admin/audit-log")
  @RequiredRoles({RoleName.ADMIN, RoleName.OFFICER, RoleName.SENIOR_OFFICER})
  public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> fetchAuditLog(
      @RequestParam(required = false) String applicationNumber,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(ApiResponse.of(auditLogService.fetch(applicationNumber, pageable)));
  }
}
