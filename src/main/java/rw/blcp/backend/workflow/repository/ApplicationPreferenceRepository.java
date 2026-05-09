package rw.blcp.backend.workflow.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.workflow.entity.Application;
import rw.blcp.backend.workflow.entity.ApplicationPreference;
import rw.blcp.backend.workflow.enums.EPreferenceKey;

public interface ApplicationPreferenceRepository extends JpaRepository<ApplicationPreference, UUID> {

    Optional<ApplicationPreference> findByApplicationAndPreferenceKey(
            Application application, EPreferenceKey preferenceKey);
}
