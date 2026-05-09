package rw.blcp.backend.workflow.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.workflow.entity.ApplicationStateTransition;

public interface ApplicationStateTransitionRepository
    extends JpaRepository<ApplicationStateTransition, UUID> {}
