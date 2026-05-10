package rw.blcp.backend.core.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.blcp.backend.core.application.entity.AuditLog;
import rw.blcp.backend.core.application.record.AuditLogResponse;
import rw.blcp.backend.core.application.repository.AuditLogRepository;

@Service
@RequiredArgsConstructor
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;

  @Transactional(readOnly = true)
  public Page<AuditLogResponse> fetch(String applicationNumber, Pageable pageable) {
    Page<AuditLog> page =
        (applicationNumber != null && !applicationNumber.isBlank())
            ? auditLogRepository.findByApplicationNumber(applicationNumber, pageable)
            : auditLogRepository.findAll(pageable);
    return page.map(this::toResponse);
  }

  private AuditLogResponse toResponse(AuditLog entry) {
    return new AuditLogResponse(
        entry.getId(),
        entry.getApplicationNumber(),
        entry.getEvent(),
        entry.getInitialState(),
        entry.getNewState(),
        entry.getActor() != null ? entry.getActor().getEmail() : null,
        entry.getComment(),
        entry.getCreatedAt());
  }
}
