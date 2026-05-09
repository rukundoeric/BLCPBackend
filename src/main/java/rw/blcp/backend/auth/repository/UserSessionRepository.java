package rw.blcp.backend.auth.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.auth.entity.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByTokenHash(String tokenHash);

    void deleteAllByUser(User user);
}
