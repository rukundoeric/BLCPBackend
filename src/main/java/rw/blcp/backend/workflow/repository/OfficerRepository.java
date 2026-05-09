package rw.blcp.backend.workflow.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.workflow.entity.Officer;
import rw.blcp.backend.workflow.enums.EOfficerLevel;

public interface OfficerRepository extends JpaRepository<Officer, UUID> {

  Optional<Officer> findByUserAndLevel(User user, EOfficerLevel level);
}
