package rw.blcp.backend.core.application.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rw.blcp.backend.core.application.entity.Application;

public interface ApplicationRepository
    extends JpaRepository<Application, UUID>, JpaSpecificationExecutor<Application> {

  Optional<Application> findByApplicationNumber(String applicationNumber);
}
