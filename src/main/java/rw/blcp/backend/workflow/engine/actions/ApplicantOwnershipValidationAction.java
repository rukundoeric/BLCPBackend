package rw.blcp.backend.workflow.engine.actions;

import org.springframework.stereotype.Component;
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.workflow.enums.EActionType;
import rw.blcp.backend.workflow.engine.Action;
import rw.blcp.backend.workflow.engine.records.TransitionContext;
import rw.blcp.backend.workflow.entity.Application;

@Component
public class ApplicantOwnershipValidationAction implements Action<Void> {

    @Override
    public EActionType getType() {
        return EActionType.APPLICANT_OWNERSHIP_VALIDATION;
    }

    @Override
    public void execute(TransitionContext ctx, Void args) {
        User actor = ctx.actor();
        Application app = ctx.application();

        boolean isGuestApplication = app.getApplicant() == null;
        boolean isGuestActor = actor == null;

        if (isGuestApplication && isGuestActor) return;

        if (!isGuestApplication && !isGuestActor
                && app.getApplicant().getId().equals(actor.getId())) return;

        throw new ApiException(ErrorCode.ACCESS_DENIED);
    }
}
