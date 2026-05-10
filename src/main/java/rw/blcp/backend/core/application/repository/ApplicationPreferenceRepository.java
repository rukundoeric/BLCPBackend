package rw.blcp.backend.core.application.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.application.entity.ApplicationPreference;
import rw.blcp.backend.core.application.enums.EPreferenceKey;

public interface ApplicationPreferenceRepository
    extends JpaRepository<ApplicationPreference, UUID> {

  Optional<ApplicationPreference> findByApplicationAndPreferenceKey(
      Application application, EPreferenceKey preferenceKey);
}
