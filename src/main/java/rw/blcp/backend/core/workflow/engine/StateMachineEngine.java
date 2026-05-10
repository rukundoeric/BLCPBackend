package rw.blcp.backend.core.workflow.engine;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.application.entity.ApplicationStateTransition;
import rw.blcp.backend.core.application.enums.EApplicationEvent;
import rw.blcp.backend.core.application.enums.EApplicationStatus;
import rw.blcp.backend.core.application.repository.ApplicationStateTransitionRepository;
import rw.blcp.backend.core.workflow.config.records.ActionDefinition;
import rw.blcp.backend.core.workflow.config.records.TransitionDefinition;
import rw.blcp.backend.core.workflow.config.records.TransitionKey;
import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateMachineEngine {

  private final Map<TransitionKey, TransitionDefinition> stateMachineTransitions;
  private final ActionRegistry actionRegistry;
  private final ApplicationStateTransitionRepository transitionRepository;

  public void execute(EApplicationEvent event, TransitionContext ctx) {
    Application app = ctx.application();
    TransitionKey key = new TransitionKey(event, app.getStatus());

    TransitionDefinition definition = stateMachineTransitions.get(key);
    if (definition == null) {
      log.warn(
          "No transition found for event={} fromState={} application={}",
          event,
          app.getStatus(),
          app.getId());
      throw new ApiException(ErrorCode.INVALID_STATE_TRANSITION);
    }

    runBreakingActions(definition, ctx);

    EApplicationStatus previousStatus = app.getStatus();
    app.setStatus(definition.toState());
    if (definition.processingLevel() != null) {
      app.setProcessingLevel(definition.processingLevel());
    }

    logTransition(app, event, previousStatus, definition.toState(), ctx);

    runNonBreakingActions(definition, ctx);
  }

  private void runBreakingActions(TransitionDefinition definition, TransitionContext ctx) {
    for (ActionDefinition action : definition.breakingActions()) {
      actionRegistry.get(action.actionType()).execute(ctx, action.args());
    }
  }

  private void runNonBreakingActions(TransitionDefinition definition, TransitionContext ctx) {
    for (ActionDefinition action : definition.nonBreakingActions()) {
      CompletableFuture.runAsync(
          () -> {
            try {
              actionRegistry.get(action.actionType()).execute(ctx, action.args());
            } catch (Exception e) {
              log.warn(
                  "Non-breaking action {} failed for application {} — {}",
                  action.actionType(),
                  ctx.application().getId(),
                  e.getMessage());
            }
          });
    }
  }

  private void logTransition(
      Application app,
      EApplicationEvent event,
      EApplicationStatus previousStatus,
      EApplicationStatus newStatus,
      TransitionContext ctx) {
    ApplicationStateTransition applicationStateTransition = new ApplicationStateTransition();
    applicationStateTransition.setApplicationNumber(app.getApplicationNumber());
    applicationStateTransition.setApplication(app);
    applicationStateTransition.setEvent(event);
    applicationStateTransition.setInitialState(previousStatus);
    applicationStateTransition.setNewState(newStatus);
    applicationStateTransition.setActor(ctx.actor());
    transitionRepository.save(applicationStateTransition);
    log.info(
        "Transition logged: application={} event={} {} -> {} actor={}",
        app.getApplicationNumber(),
        event,
        previousStatus,
        newStatus,
        ctx.actor() != null ? ctx.actor().getEmail() : "guest");
  }
}
