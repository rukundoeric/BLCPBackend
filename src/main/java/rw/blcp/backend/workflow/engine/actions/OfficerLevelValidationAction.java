package rw.blcp.backend.workflow.engine.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.workflow.config.records.OfficerLevelValidationArgs;
import rw.blcp.backend.workflow.engine.Action;
import rw.blcp.backend.workflow.engine.records.TransitionContext;
import rw.blcp.backend.workflow.enums.EActionType;
import rw.blcp.backend.workflow.repository.OfficerRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfficerLevelValidationAction implements Action<OfficerLevelValidationArgs> {

  private final OfficerRepository officerRepository;

  @Override
  public EActionType getType() {
    return EActionType.OFFICER_LEVEL_VALIDATION;
  }

  @Override
  public void execute(TransitionContext ctx, OfficerLevelValidationArgs args) {
    /* This is validate if the  officer triggering this event have the level that allows them to do so */
    if (ctx.actor() == null) {
      throw new ApiException(ErrorCode.ACCESS_DENIED);
    }

    officerRepository
        .findByUserAndLevel(ctx.actor(), args.requiredLevel())
        .orElseThrow(
            () -> {
              log.warn(
                  "User {} does not hold officer level {} required for this transition",
                  ctx.actor().getEmail(),
                  args.requiredLevel());
              return new ApiException(ErrorCode.ACCESS_DENIED);
            });
  }
}
