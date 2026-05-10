package rw.blcp.backend.core.application.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.core.application.entity.ApplicationStateTransition;

public interface ApplicationStateTransitionRepository
    extends JpaRepository<ApplicationStateTransition, UUID> {}
