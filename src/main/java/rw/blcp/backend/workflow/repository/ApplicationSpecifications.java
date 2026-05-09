package rw.blcp.backend.workflow.repository;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import rw.blcp.backend.auth.RoleName;
import rw.blcp.backend.auth.entity.Role;
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.core.RecordState;
import rw.blcp.backend.workflow.dto.ApplicationFilter;
import rw.blcp.backend.workflow.entity.Application;
import rw.blcp.backend.workflow.enums.EOfficerLevel;

public class ApplicationSpecifications {

  public static Specification<Application> withFilter(ApplicationFilter filter, User actor) {
    return Specification.where(onlyActive())
        .and(byApplicationNumber(filter.applicationNumber()))
        .and(byBankName(filter.bankName()))
        .and(byBankType(filter.bankType()))
        .and(byStatus(filter.status()))
        .and(byApplicantEmail(filter.applicantEmail()))
        .and(byVisibility(actor));
  }

  private static Specification<Application> onlyActive() {
    return (root, query, cb) -> cb.equal(root.get("state"), RecordState.ACTIVE);
  }

  private static Specification<Application> byApplicationNumber(String applicationNumber) {
    if (applicationNumber == null || applicationNumber.isBlank()) return null;
    return (root, query, cb) -> cb.equal(root.get("applicationNumber"), applicationNumber);
  }

  private static Specification<Application> byBankName(String bankName) {
    if (bankName == null || bankName.isBlank()) return null;
    return (root, query, cb) ->
        cb.like(cb.lower(root.get("bankName")), "%" + bankName.toLowerCase() + "%");
  }

  private static Specification<Application> byBankType(String bankType) {
    if (bankType == null || bankType.isBlank()) return null;
    return (root, query, cb) -> cb.equal(root.get("bankType"), bankType);
  }

  private static Specification<Application> byStatus(
      rw.blcp.backend.workflow.enums.EApplicationStatus status) {
    if (status == null) return null;
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }

  private static Specification<Application> byApplicantEmail(String applicantEmail) {
    if (applicantEmail == null || applicantEmail.isBlank()) return null;
    return (root, query, cb) ->
        cb.like(cb.lower(root.get("applicantEmail")), "%" + applicantEmail.toLowerCase() + "%");
  }

  private static Specification<Application> byVisibility(User actor) {
    Set<RoleName> roles = actor.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

    if (roles.contains(RoleName.ADMIN)) return null;

    if (roles.contains(RoleName.SENIOR_OFFICER)) {
      return (root, query, cb) -> cb.equal(root.get("processingLevel"), EOfficerLevel.LEVEL_2);
    }

    if (roles.contains(RoleName.OFFICER)) {
      return (root, query, cb) -> cb.equal(root.get("processingLevel"), EOfficerLevel.LEVEL_1);
    }

    // APPLICANT — only their own
    return (root, query, cb) -> cb.equal(root.get("applicant"), actor);
  }
}
