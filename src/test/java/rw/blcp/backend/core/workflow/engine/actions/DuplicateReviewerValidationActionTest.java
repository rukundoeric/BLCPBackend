package rw.blcp.backend.core.workflow.engine.actions;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.blcp.backend.core.application.entity.ApplicationPreference;
import rw.blcp.backend.core.application.enums.EPreferenceKey;
import rw.blcp.backend.core.application.repository.ApplicationPreferenceRepository;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.fixtures.TestFixtures;

@ExtendWith(MockitoExtension.class)
class DuplicateReviewerValidationActionTest {

  @Mock ApplicationPreferenceRepository preferenceRepository;
  @InjectMocks DuplicateReviewerValidationAction action;

  @Test
  void execute_whenActorIsTheSamePersonWhoDidLevel1Review_throwsReviewerApproverConflict() {
    var actor = TestFixtures.userWithRoles(RoleName.SENIOR_OFFICER);
    // Give actor a fixed ID so we can reference it
    var actorId = UUID.randomUUID();
    setId(actor, actorId);

    var application = TestFixtures.submittedApplication();
    var preference = preferenceWith(actorId.toString());

    when(preferenceRepository.findByApplicationAndPreferenceKey(
            application, EPreferenceKey.LEVEL1_OFFICER_ID))
        .thenReturn(Optional.of(preference));

    var ctx = new TransitionContext(application, actor, null, List.of());

    assertThatThrownBy(() -> action.execute(ctx, null))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.REVIEWER_APPROVER_CONFLICT);
  }

  @Test
  void execute_whenActorIsDifferentFromLevel1Reviewer_passes() {
    var actor = TestFixtures.userWithRoles(RoleName.SENIOR_OFFICER);
    setId(actor, UUID.randomUUID());

    var application = TestFixtures.submittedApplication();
    var preference = preferenceWith(UUID.randomUUID().toString()); // different ID

    when(preferenceRepository.findByApplicationAndPreferenceKey(
            application, EPreferenceKey.LEVEL1_OFFICER_ID))
        .thenReturn(Optional.of(preference));

    var ctx = new TransitionContext(application, actor, null, List.of());

    assertThatNoException().isThrownBy(() -> action.execute(ctx, null));
  }

  @Test
  void execute_whenNoLevel1ReviewerRecordedYet_passes() {
    var actor = TestFixtures.userWithRoles(RoleName.SENIOR_OFFICER);
    var application = TestFixtures.submittedApplication();

    when(preferenceRepository.findByApplicationAndPreferenceKey(
            application, EPreferenceKey.LEVEL1_OFFICER_ID))
        .thenReturn(Optional.empty());

    var ctx = new TransitionContext(application, actor, null, List.of());

    assertThatNoException().isThrownBy(() -> action.execute(ctx, null));
  }

  private ApplicationPreference preferenceWith(String value) {
    ApplicationPreference pref = new ApplicationPreference();
    pref.setValue(value);
    return pref;
  }

  private void setId(User user, UUID id) {
    // BaseEntity has the id field — we need reflection to set it in tests
    try {
      var field = user.getClass().getSuperclass().getDeclaredField("id");
      field.setAccessible(true);
      field.set(user, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
