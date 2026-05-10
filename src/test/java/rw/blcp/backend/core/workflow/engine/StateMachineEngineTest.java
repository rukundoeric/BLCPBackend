package rw.blcp.backend.core.workflow.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.application.enums.EApplicationEvent;
import rw.blcp.backend.core.application.enums.EApplicationStatus;
import rw.blcp.backend.core.application.repository.AuditLogRepository;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;
import rw.blcp.backend.core.workflow.config.records.ActionDefinition;
import rw.blcp.backend.core.workflow.config.records.TransitionDefinition;
import rw.blcp.backend.core.workflow.config.records.TransitionKey;
import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.core.workflow.enums.EActionType;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.fixtures.TestFixtures;

@ExtendWith(MockitoExtension.class)
class StateMachineEngineTest {

  @Mock AuditLogRepository transitionRepository;

  private StateMachineEngine engine;
  private Application application;
  private TransitionContext ctx;

  @BeforeEach
  void setUp() {
    application = TestFixtures.newApplication();
    ctx = new TransitionContext(application, TestFixtures.userWithRoles(), null, List.of());
  }

  @Test
  void execute_withNoMatchingTransition_throwsInvalidStateTransition() {
    engine = engineWith(Map.of());

    assertThatThrownBy(() -> engine.execute(EApplicationEvent.APPLY, ctx))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
  }

  @Test
  void execute_validTransition_mutatesApplicationStatus() {
    engine = engineWith(transitionMap(List.of(), List.of()));

    engine.execute(EApplicationEvent.APPLY, ctx);

    assertThat(application.getStatus()).isEqualTo(EApplicationStatus.SUBMITTED);
  }

  @Test
  void execute_validTransition_mutatesProcessingLevel() {
    engine = engineWith(transitionMap(List.of(), List.of()));

    engine.execute(EApplicationEvent.APPLY, ctx);

    assertThat(application.getProcessingLevel()).isEqualTo(EOfficerLevel.LEVEL_1);
  }

  @Test
  void execute_validTransition_logsTransitionToRepository() {
    engine = engineWith(transitionMap(List.of(), List.of()));
    when(transitionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    engine.execute(EApplicationEvent.APPLY, ctx);

    verify(transitionRepository).save(any());
  }

  @Test
  void execute_whenBreakingActionFails_statusIsNotMutated() {
    Action<Object> failingAction = mock(Action.class);
    when(failingAction.getType()).thenReturn(EActionType.OFFICER_LEVEL_VALIDATION);
    doThrow(new ApiException(ErrorCode.ACCESS_DENIED)).when(failingAction).execute(any(), any());

    ActionRegistry registry = new ActionRegistry(List.of(failingAction));
    engine =
        new StateMachineEngine(
            transitionMap(
                List.of(ActionDefinition.of(EActionType.OFFICER_LEVEL_VALIDATION)), List.of()),
            registry,
            transitionRepository);

    assertThatThrownBy(() -> engine.execute(EApplicationEvent.APPLY, ctx))
        .isInstanceOf(ApiException.class);

    assertThat(application.getStatus())
        .as("Status must not change when a breaking action fails")
        .isEqualTo(EApplicationStatus.NEW);
  }

  @Test
  void execute_whenNonBreakingActionFails_transitionStillCompletes() {
    // Empty registry — the async handler catches the unregistered-action failure synchronously
    engine =
        new StateMachineEngine(
            transitionMap(List.of(), List.of(ActionDefinition.of(EActionType.NOTIFICATION))),
            new ActionRegistry(List.of()),
            transitionRepository);
    when(transitionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    engine.execute(EApplicationEvent.APPLY, ctx);

    assertThat(application.getStatus())
        .as("Status must be mutated even when a non-breaking action fails")
        .isEqualTo(EApplicationStatus.SUBMITTED);
  }

  private StateMachineEngine engineWith(Map<TransitionKey, TransitionDefinition> transitions) {
    return new StateMachineEngine(transitions, new ActionRegistry(List.of()), transitionRepository);
  }

  private Map<TransitionKey, TransitionDefinition> transitionMap(
      List<ActionDefinition> breakingActions, List<ActionDefinition> nonBreakingActions) {
    return Map.of(
        new TransitionKey(EApplicationEvent.APPLY, EApplicationStatus.NEW),
        new TransitionDefinition(
            EApplicationEvent.APPLY,
            EApplicationStatus.NEW,
            EApplicationStatus.SUBMITTED,
            EOfficerLevel.LEVEL_1,
            breakingActions,
            nonBreakingActions));
  }
}
