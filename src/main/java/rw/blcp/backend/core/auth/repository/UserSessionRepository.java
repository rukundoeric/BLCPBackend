package rw.blcp.backend.core.auth.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.auth.entity.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

  Optional<UserSession> findByTokenHash(String tokenHash);

  void deleteAllByUser(User user);

  boolean existsByUser(User user);
}
