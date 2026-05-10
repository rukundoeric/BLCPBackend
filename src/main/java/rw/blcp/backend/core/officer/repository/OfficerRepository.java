package rw.blcp.backend.core.officer.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.officer.entity.Officer;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;

public interface OfficerRepository extends JpaRepository<Officer, UUID> {

  Optional<Officer> findByUserAndLevel(User user, EOfficerLevel level);

  boolean existsByUserAndLevel(User user, EOfficerLevel level);
}
