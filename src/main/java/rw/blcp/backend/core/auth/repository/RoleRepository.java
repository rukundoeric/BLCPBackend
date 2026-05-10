package rw.blcp.backend.core.auth.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.auth.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

  Optional<Role> findByName(RoleName name);
}
