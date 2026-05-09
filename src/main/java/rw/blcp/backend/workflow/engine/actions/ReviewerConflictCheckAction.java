package rw.blcp.backend.workflow.engine.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.workflow.enums.EActionType;
import rw.blcp.backend.workflow.engine.Action;
import rw.blcp.backend.workflow.engine.records.TransitionContext;
import rw.blcp.backend.workflow.enums.EPreferenceKey;
import rw.blcp.backend.workflow.repository.ApplicationPreferenceRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewerConflictCheckAction implements Action<Void> {

    private final ApplicationPreferenceRepository preferenceRepository;

    @Override
    public EActionType getType() {
        return EActionType.REVIEWER_CONFLICT_CHECK;
    }

    @Override
    public void execute(TransitionContext ctx, Void args) {
        preferenceRepository
                .findByApplicationAndPreferenceKey(
                        ctx.application(), EPreferenceKey.LEVEL1_OFFICER_ID)
                .ifPresent(pref -> {
                    if (pref.getValue().equals(ctx.actor().getId().toString())) {
                        log.warn(
                                "Reviewer conflict: user {} is both level-1 reviewer and final approver for application {}",
                                ctx.actor().getEmail(),
                                ctx.application().getApplicationNumber());
                        throw new ApiException(ErrorCode.REVIEWER_APPROVER_CONFLICT);
                    }
                });
    }
}
