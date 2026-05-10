package rw.blcp.backend.core.officer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.auth.repository.RoleRepository;
import rw.blcp.backend.core.auth.repository.UserRepository;
import rw.blcp.backend.core.officer.entity.Officer;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;
import rw.blcp.backend.core.officer.record.AssignOfficerRoleRequest;
import rw.blcp.backend.core.officer.record.CreateOfficerRequest;
import rw.blcp.backend.core.officer.repository.OfficerRepository;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.fixtures.TestFixtures;

@ExtendWith(MockitoExtension.class)
class OfficerServiceTest {

  @Mock UserRepository userRepository;
  @Mock RoleRepository roleRepository;
  @Mock OfficerRepository officerRepository;
  @Mock PasswordEncoder passwordEncoder;
  @InjectMocks OfficerService officerService;

  @Test
  void createOfficer_savesUserWithHashedPassword() {
    var role = TestFixtures.roleWith(RoleName.OFFICER);
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(roleRepository.findByName(RoleName.OFFICER)).thenReturn(Optional.of(role));
    when(passwordEncoder.encode("Test@1234")).thenReturn("hashed");
    when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(officerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    officerService.createOfficer(
        new CreateOfficerRequest(
            "officer@nbr.rw",
            "Bob",
            "Nkuru",
            "Test@1234",
            RoleName.OFFICER,
            EOfficerLevel.LEVEL_1));

    ArgumentCaptor<User> userCaptor = forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getPassword()).isEqualTo("hashed");
    assertThat(userCaptor.getValue().getRoles()).anyMatch(r -> r.getName() == RoleName.OFFICER);
  }

  @Test
  void createOfficer_savesOfficerWithCorrectLevelAndRole() {
    var role = TestFixtures.roleWith(RoleName.SENIOR_OFFICER);
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(roleRepository.findByName(RoleName.SENIOR_OFFICER)).thenReturn(Optional.of(role));
    when(passwordEncoder.encode(any())).thenReturn("hashed");
    when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(officerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    officerService.createOfficer(
        new CreateOfficerRequest(
            "senior@nbr.rw",
            "Claire",
            "Uwimana",
            "Test@1234",
            RoleName.SENIOR_OFFICER,
            EOfficerLevel.LEVEL_2));

    ArgumentCaptor<Officer> officerCaptor = forClass(Officer.class);
    verify(officerRepository).save(officerCaptor.capture());
    assertThat(officerCaptor.getValue().getLevel()).isEqualTo(EOfficerLevel.LEVEL_2);
    assertThat(officerCaptor.getValue().getRole().getName()).isEqualTo(RoleName.SENIOR_OFFICER);
  }

  @Test
  void createOfficer_whenEmailAlreadyInUse_throwsEmailAlreadyInUse() {
    when(userRepository.existsByEmail("taken@nbr.rw")).thenReturn(true);

    assertThatThrownBy(
            () ->
                officerService.createOfficer(
                    new CreateOfficerRequest(
                        "taken@nbr.rw",
                        "A",
                        "B",
                        "Test@1234",
                        RoleName.OFFICER,
                        EOfficerLevel.LEVEL_1)))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.EMAIL_ALREADY_IN_USE);

    verify(userRepository, never()).save(any());
  }

  @Test
  void createOfficer_withAdminRole_throwsValidationFailed() {
    assertThatThrownBy(
            () ->
                officerService.createOfficer(
                    new CreateOfficerRequest(
                        "admin2@nbr.rw",
                        "A",
                        "B",
                        "Test@1234",
                        RoleName.ADMIN,
                        EOfficerLevel.LEVEL_1)))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.VALIDATION_FAILED);
  }

  @Test
  void assignOfficerRole_addsRoleToExistingUserAndCreatesOfficerRecord() {
    var user = TestFixtures.userWithRoles(RoleName.APPLICANT);
    var role = TestFixtures.roleWith(RoleName.OFFICER);
    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    when(officerRepository.existsByUserAndLevel(user, EOfficerLevel.LEVEL_1)).thenReturn(false);
    when(roleRepository.findByName(RoleName.OFFICER)).thenReturn(Optional.of(role));
    when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(officerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    officerService.assignOfficerRole(
        UUID.randomUUID(), new AssignOfficerRoleRequest(RoleName.OFFICER, EOfficerLevel.LEVEL_1));

    ArgumentCaptor<User> userCaptor = forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getRoles()).anyMatch(r -> r.getName() == RoleName.OFFICER);
  }

  @Test
  void assignOfficerRole_whenUserNotFound_throwsUserNotFound() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                officerService.assignOfficerRole(
                    UUID.randomUUID(),
                    new AssignOfficerRoleRequest(RoleName.OFFICER, EOfficerLevel.LEVEL_1)))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.USER_NOT_FOUND);
  }

  @Test
  void assignOfficerRole_whenUserAlreadyOfficerAtThatLevel_throwsOfficerAlreadyExists() {
    var user = TestFixtures.userWithRoles(RoleName.OFFICER);
    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    when(officerRepository.existsByUserAndLevel(user, EOfficerLevel.LEVEL_1)).thenReturn(true);

    assertThatThrownBy(
            () ->
                officerService.assignOfficerRole(
                    UUID.randomUUID(),
                    new AssignOfficerRoleRequest(RoleName.OFFICER, EOfficerLevel.LEVEL_1)))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.OFFICER_ALREADY_EXISTS);

    verify(officerRepository, never()).save(any());
  }
}
