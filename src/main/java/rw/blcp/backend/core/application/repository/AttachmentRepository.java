package rw.blcp.backend.core.application.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.core.application.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {}
