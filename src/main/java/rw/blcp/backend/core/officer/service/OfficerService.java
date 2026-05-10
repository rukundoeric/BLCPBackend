package rw.blcp.backend.core.officer.service;

import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.auth.entity.Role;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.auth.repository.RoleRepository;
import rw.blcp.backend.core.auth.repository.UserRepository;
import rw.blcp.backend.core.officer.entity.Officer;
import rw.blcp.backend.core.officer.record.AssignOfficerRoleRequest;
import rw.blcp.backend.core.officer.record.CreateOfficerRequest;
import rw.blcp.backend.core.officer.record.OfficerResponse;
import rw.blcp.backend.core.officer.repository.OfficerRepository;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficerService {

  private static final Set<RoleName> OFFICER_ROLES =
      Set.of(RoleName.OFFICER, RoleName.SENIOR_OFFICER);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final OfficerRepository officerRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public OfficerResponse createOfficer(CreateOfficerRequest request) {
    validateOfficerRole(request.roleName());

    if (userRepository.existsByEmail(request.email())) {
      throw new ApiException(ErrorCode.EMAIL_ALREADY_IN_USE);
    }

    Role role = findRole(request.roleName());

    User user = new User();
    user.setEmail(request.email());
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.getRoles().add(role);
    userRepository.save(user);

    Officer officer = new Officer();
    officer.setUser(user);
    officer.setRole(role);
    officer.setLevel(request.officerLevel());
    officerRepository.save(officer);

    log.info(
        "Officer created: {} role={} level={}",
        user.getEmail(),
        request.roleName(),
        request.officerLevel());
    return toResponse(officer);
  }

  @Transactional
  public OfficerResponse assignOfficerRole(UUID userId, AssignOfficerRoleRequest request) {
    validateOfficerRole(request.roleName());

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    if (officerRepository.existsByUserAndLevel(user, request.officerLevel())) {
      throw new ApiException(ErrorCode.OFFICER_ALREADY_EXISTS);
    }

    Role role = findRole(request.roleName());
    user.getRoles().add(role);
    userRepository.save(user);

    Officer officer = new Officer();
    officer.setUser(user);
    officer.setRole(role);
    officer.setLevel(request.officerLevel());
    officerRepository.save(officer);

    log.info(
        "Role {} / level {} assigned to user: {}",
        request.roleName(),
        request.officerLevel(),
        user.getEmail());
    return toResponse(officer);
  }

  private void validateOfficerRole(RoleName roleName) {
    if (!OFFICER_ROLES.contains(roleName)) {
      throw new ApiException(
          ErrorCode.VALIDATION_FAILED, roleName + " is not a valid officer role");
    }
  }

  private Role findRole(RoleName roleName) {
    return roleRepository
        .findByName(roleName)
        .orElseThrow(
            () -> new ApiException(ErrorCode.INTERNAL_ERROR, "Role not available: " + roleName));
  }

  private OfficerResponse toResponse(Officer officer) {
    User user = officer.getUser();
    return new OfficerResponse(
        officer.getId(),
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        officer.getLevel());
  }
}
