package rw.blcp.backend.core.workflow.engine.actions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rw.blcp.backend.core.workflow.engine.Action;
import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.core.workflow.enums.EActionType;

@Slf4j
@Component
public class NotificationAction implements Action<Void> {

  @Override
  public EActionType getType() {
    return EActionType.NOTIFICATION;
  }

  @Override
  public void execute(TransitionContext ctx, Void args) {
    /* we can consider triggering actual Email/sms notification later */
    log.info(
        "[NOTIFICATION] Application {} ('{}') transitioned to {} — notifying {}",
        ctx.application().getApplicationNumber(),
        ctx.application().getBankName(),
        ctx.application().getStatus(),
        ctx.application().getApplicantEmail());
  }
}
