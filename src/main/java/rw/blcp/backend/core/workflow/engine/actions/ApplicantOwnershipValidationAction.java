package rw.blcp.backend.core.workflow.engine.actions;

import org.springframework.stereotype.Component;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.workflow.engine.Action;
import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.core.workflow.enums.EActionType;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;

@Component
public class ApplicantOwnershipValidationAction implements Action<Void> {

  @Override
  public EActionType getType() {
    return EActionType.APPLICANT_OWNERSHIP_VALIDATION;
  }

  @Override
  public void execute(TransitionContext ctx, Void args) {
    User actor = ctx.actor();
    User applicant = ctx.application().getApplicant();
    if (actor == null || applicant == null || !actor.getId().equals(applicant.getId())) {
      throw new ApiException(ErrorCode.ACCESS_DENIED);
    }
  }
}
