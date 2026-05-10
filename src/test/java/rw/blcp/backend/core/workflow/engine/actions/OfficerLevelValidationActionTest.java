package rw.blcp.backend.core.workflow.engine.actions;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.officer.entity.Officer;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;
import rw.blcp.backend.core.officer.repository.OfficerRepository;
import rw.blcp.backend.core.workflow.config.records.OfficerLevelValidationArgs;
import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.fixtures.TestFixtures;

@ExtendWith(MockitoExtension.class)
class OfficerLevelValidationActionTest {

  @Mock OfficerRepository officerRepository;
  @InjectMocks OfficerLevelValidationAction action;

  @Test
  void execute_whenActorHoldsRequiredLevel_passes() {
    var actor = TestFixtures.userWithRoles(RoleName.OFFICER);
    var application = TestFixtures.submittedApplication();
    var args = new OfficerLevelValidationArgs(EOfficerLevel.LEVEL_1);

    when(officerRepository.findByUserAndLevel(actor, EOfficerLevel.LEVEL_1))
        .thenReturn(Optional.of(new Officer()));

    var ctx = new TransitionContext(application, actor, null, List.of());

    assertThatNoException().isThrownBy(() -> action.execute(ctx, args));
  }

  @Test
  void execute_whenActorDoesNotHoldRequiredLevel_throwsAccessDenied() {
    var actor = TestFixtures.userWithRoles(RoleName.OFFICER);
    var application = TestFixtures.submittedApplication();
    var args = new OfficerLevelValidationArgs(EOfficerLevel.LEVEL_2);

    when(officerRepository.findByUserAndLevel(actor, EOfficerLevel.LEVEL_2))
        .thenReturn(Optional.empty());

    var ctx = new TransitionContext(application, actor, null, List.of());

    assertThatThrownBy(() -> action.execute(ctx, args))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.ACCESS_DENIED);
  }

  @Test
  void execute_whenNoActorPresent_throwsAccessDenied() {
    var application = TestFixtures.submittedApplication();
    var args = new OfficerLevelValidationArgs(EOfficerLevel.LEVEL_1);

    var ctx = new TransitionContext(application, null, null, List.of());

    assertThatThrownBy(() -> action.execute(ctx, args))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.ACCESS_DENIED);
  }
}
