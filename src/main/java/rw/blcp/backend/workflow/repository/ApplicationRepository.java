package rw.blcp.backend.workflow.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.workflow.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findAllByApplicant(User applicant);
}
