package rw.blcp.backend.core.application.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.application.entity.AuditLog;

public interface AuditLogRepository extends Repository<AuditLog, UUID> {

  AuditLog save(AuditLog entry);

  List<AuditLog> findByApplication(Application application);

  Page<AuditLog> findAll(Pageable pageable);

  Page<AuditLog> findByApplicationNumber(String applicationNumber, Pageable pageable);
}
