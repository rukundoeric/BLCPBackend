package rw.blcp.backend.core.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.application.enums.EApplicationEvent;
import rw.blcp.backend.core.application.record.CreateApplicationRequest;
import rw.blcp.backend.core.application.record.TakeActionRequest;
import rw.blcp.backend.core.application.repository.ApplicationRepository;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.workflow.engine.StateMachineEngine;
import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.fixtures.TestFixtures;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

  @Mock ApplicationRepository applicationRepository;
  @Mock StateMachineEngine stateMachineEngine;
  @InjectMocks ApplicationService applicationService;

  @Test
  void create_withAuthenticatedUser_usesUserProfileOverRequestFields() {
    var actor = TestFixtures.userWithRoles(RoleName.APPLICANT);
    actor.setEmail("actor@example.com");
    actor.setFirstName("Actor");
    actor.setLastName("Name");
    when(applicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(applicationRepository.count()).thenReturn(0L);

    CreateApplicationRequest request =
        new CreateApplicationRequest(
            "request@example.com", "Request", "Name", "My Bank", "COMMERCIAL", null, null);

    applicationService.create(request, List.of(), actor);

    ArgumentCaptor<Application> captor = forClass(Application.class);
    verify(applicationRepository).save(captor.capture());
    assertThat(captor.getValue().getApplicantEmail()).isEqualTo("actor@example.com");
    assertThat(captor.getValue().getApplicantFirstName()).isEqualTo("Actor");
  }

  @Test
  void create_withGuestUser_usesRequestFields() {
    when(applicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(applicationRepository.count()).thenReturn(0L);

    CreateApplicationRequest request =
        new CreateApplicationRequest(
            "guest@example.com", "Guest", "Applicant", "Guest Bank", "COMMERCIAL", null, null);

    applicationService.create(request, List.of(), null);

    ArgumentCaptor<Application> captor = forClass(Application.class);
    verify(applicationRepository).save(captor.capture());
    assertThat(captor.getValue().getApplicantEmail()).isEqualTo("guest@example.com");
    assertThat(captor.getValue().getApplicantFirstName()).isEqualTo("Guest");
  }

  @Test
  void create_firesApplyEventOnTheStateMachine() {
    when(applicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(applicationRepository.count()).thenReturn(0L);

    CreateApplicationRequest request =
        new CreateApplicationRequest(
            "jane@example.com", "Jane", "Doe", "Sunrise Bank", "COMMERCIAL", null, null);

    applicationService.create(request, List.of(), null);

    ArgumentCaptor<TransitionContext> ctxCaptor = forClass(TransitionContext.class);
    verify(stateMachineEngine).execute(eq(EApplicationEvent.APPLY), ctxCaptor.capture());
    assertThat(ctxCaptor.getValue().application().getBankName()).isEqualTo("Sunrise Bank");
  }

  @Test
  void takeAction_withRejectAndNoComment_throwsValidationFailed() {
    when(applicationRepository.findByApplicationNumber("APP-2026-0001"))
        .thenReturn(Optional.of(TestFixtures.submittedApplication()));

    assertThatThrownBy(
            () ->
                applicationService.takeAction(
                    "APP-2026-0001",
                    new TakeActionRequest(EApplicationEvent.REJECT, null),
                    TestFixtures.userWithRoles(RoleName.OFFICER)))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.VALIDATION_FAILED);
  }

  @Test
  void takeAction_withRejectAndBlankComment_throwsValidationFailed() {
    when(applicationRepository.findByApplicationNumber("APP-2026-0001"))
        .thenReturn(Optional.of(TestFixtures.submittedApplication()));

    assertThatThrownBy(
            () ->
                applicationService.takeAction(
                    "APP-2026-0001",
                    new TakeActionRequest(EApplicationEvent.REJECT, "   "),
                    TestFixtures.userWithRoles(RoleName.OFFICER)))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.VALIDATION_FAILED);
  }

  @Test
  void takeAction_withUnknownApplicationNumber_throwsApplicationNotFound() {
    when(applicationRepository.findByApplicationNumber(any())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                applicationService.takeAction(
                    "APP-0000-9999",
                    new TakeActionRequest(EApplicationEvent.APPROVE, null),
                    TestFixtures.userWithRoles(RoleName.OFFICER)))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.APPLICATION_NOT_FOUND);
  }

  @Test
  void takeAction_delegatesToEngineWithActorAndComment() {
    var actor = TestFixtures.userWithRoles(RoleName.OFFICER);
    var application = TestFixtures.submittedApplication();
    when(applicationRepository.findByApplicationNumber("APP-2026-0001"))
        .thenReturn(Optional.of(application));

    applicationService.takeAction(
        "APP-2026-0001", new TakeActionRequest(EApplicationEvent.APPROVE, "Verified"), actor);

    ArgumentCaptor<TransitionContext> ctxCaptor = forClass(TransitionContext.class);
    verify(stateMachineEngine).execute(eq(EApplicationEvent.APPROVE), ctxCaptor.capture());
    assertThat(ctxCaptor.getValue().actor()).isEqualTo(actor);
    assertThat(ctxCaptor.getValue().comment()).isEqualTo("Verified");
  }
}
