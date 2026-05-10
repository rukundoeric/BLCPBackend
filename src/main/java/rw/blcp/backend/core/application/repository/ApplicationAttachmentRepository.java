package rw.blcp.backend.core.application.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.application.entity.ApplicationAttachment;

public interface ApplicationAttachmentRepository
    extends JpaRepository<ApplicationAttachment, UUID> {

  List<ApplicationAttachment> findByApplication(Application application);
}
