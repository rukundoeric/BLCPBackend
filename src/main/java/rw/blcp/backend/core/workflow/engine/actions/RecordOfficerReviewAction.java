package rw.blcp.backend.core.workflow.engine.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.application.entity.ApplicationPreference;
import rw.blcp.backend.core.application.enums.EPreferenceKey;
import rw.blcp.backend.core.application.repository.ApplicationPreferenceRepository;
import rw.blcp.backend.core.workflow.config.records.RecordOfficerReviewArgs;
import rw.blcp.backend.core.workflow.engine.Action;
import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.core.workflow.enums.EActionType;

@Component
@RequiredArgsConstructor
public class RecordOfficerReviewAction implements Action<RecordOfficerReviewArgs> {

  private final ApplicationPreferenceRepository preferenceRepository;

  @Override
  public EActionType getType() {
    return EActionType.RECORD_OFFICER_REVIEW;
  }

  @Override
  public void execute(TransitionContext ctx, RecordOfficerReviewArgs args) {
    // This action is to record application preferences
    upsert(ctx.application(), args.officerIdKey(), ctx.actor().getId().toString());
    upsert(ctx.application(), args.commentKey(), ctx.comment() != null ? ctx.comment() : "");
  }

  private void upsert(Application app, EPreferenceKey key, String value) {
    ApplicationPreference pref =
        preferenceRepository
            .findByApplicationAndPreferenceKey(app, key)
            .orElse(new ApplicationPreference());
    pref.setApplication(app);
    pref.setPreferenceKey(key);
    pref.setValue(value);
    preferenceRepository.save(pref);
  }
}
