package rw.blcp.backend.core.application.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.application.entity.ApplicationAttachment;

public interface ApplicationAttachmentRepository
    extends JpaRepository<ApplicationAttachment, UUID> {

  List<ApplicationAttachment> findByApplication(Application application);

  @Query(
      "SELECT MAX(a.submissionVersion) FROM ApplicationAttachment a WHERE a.application = :application")
  Optional<Integer> findMaxSubmissionVersionByApplication(
      @Param("application") Application application);
}
