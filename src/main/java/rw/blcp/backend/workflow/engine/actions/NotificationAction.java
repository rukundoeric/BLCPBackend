package rw.blcp.backend.workflow.engine.actions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import rw.blcp.backend.workflow.enums.EActionType;
import rw.blcp.backend.workflow.engine.Action;
import rw.blcp.backend.workflow.engine.records.TransitionContext;

@Slf4j
@Component
public class NotificationAction implements Action<Void> {

    @Override
    public EActionType getType() {
        return EActionType.NOTIFICATION;
    }

    @Async
    @Override
    public void execute(TransitionContext ctx, Void args) {
        log.info(
                "[NOTIFICATION] Application {} ('{}') transitioned to {} — notifying {}",
                ctx.application().getApplicationNumber(),
                ctx.application().getBankName(),
                ctx.application().getStatus(),
                ctx.application().getApplicantEmail());
    }
}
