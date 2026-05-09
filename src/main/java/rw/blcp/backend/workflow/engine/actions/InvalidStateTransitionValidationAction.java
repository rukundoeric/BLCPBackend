package rw.blcp.backend.workflow.engine.actions;

import org.springframework.stereotype.Component;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.workflow.enums.EActionType;
import rw.blcp.backend.workflow.engine.Action;
import rw.blcp.backend.workflow.engine.records.TransitionContext;
import rw.blcp.backend.workflow.enums.EApplicationStatus;

@Component
public class InvalidStateTransitionValidationAction implements Action<Void> {

    @Override
    public EActionType getType() {
        return EActionType.INVALID_STATE_TRANSITION_VALIDATION;
    }

    @Override
    public void execute(TransitionContext ctx, Void args) {
        EApplicationStatus status = ctx.application().getStatus();
        if (status == EApplicationStatus.APPROVED || status == EApplicationStatus.REJECTED) {
            throw new ApiException(ErrorCode.FINAL_STATE_IMMUTABLE);
        }
    }
}
