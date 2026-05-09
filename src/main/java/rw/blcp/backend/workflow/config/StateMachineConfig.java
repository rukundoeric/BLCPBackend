package rw.blcp.backend.workflow.config;

import static rw.blcp.backend.workflow.config.records.ActionDefinition.of;
import static rw.blcp.backend.workflow.enums.EActionType.INVALID_STATE_TRANSITION_VALIDATION;
import static rw.blcp.backend.workflow.enums.EActionType.NOTIFICATION;
import static rw.blcp.backend.workflow.enums.EActionType.OFFICER_LEVEL_VALIDATION;
import static rw.blcp.backend.workflow.enums.EActionType.RECORD_OFFICER_REVIEW;
import static rw.blcp.backend.workflow.enums.EActionType.REVIEWER_CONFLICT_CHECK;
import static rw.blcp.backend.workflow.enums.EApplicationEvent.APPLY;
import static rw.blcp.backend.workflow.enums.EApplicationEvent.APPROVE;
import static rw.blcp.backend.workflow.enums.EApplicationEvent.REJECT;
import static rw.blcp.backend.workflow.enums.EApplicationStatus.NEW;
import static rw.blcp.backend.workflow.enums.EApplicationStatus.PENDING_FINAL_APPROVAL;
import static rw.blcp.backend.workflow.enums.EApplicationStatus.REJECTED;
import static rw.blcp.backend.workflow.enums.EApplicationStatus.SUBMITTED;
import static rw.blcp.backend.workflow.enums.EApplicationStatus.APPROVED;
import static rw.blcp.backend.workflow.enums.EOfficerLevel.LEVEL_1;
import static rw.blcp.backend.workflow.enums.EOfficerLevel.LEVEL_2;
import static rw.blcp.backend.workflow.enums.EPreferenceKey.LEVEL1_OFFICER_COMMENT;
import static rw.blcp.backend.workflow.enums.EPreferenceKey.LEVEL1_OFFICER_ID;
import static rw.blcp.backend.workflow.enums.EPreferenceKey.LEVEL2_OFFICER_COMMENT;
import static rw.blcp.backend.workflow.enums.EPreferenceKey.LEVEL2_OFFICER_ID;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rw.blcp.backend.workflow.config.records.OfficerLevelValidationArgs;
import rw.blcp.backend.workflow.config.records.RecordOfficerReviewArgs;
import rw.blcp.backend.workflow.config.records.TransitionDefinition;
import rw.blcp.backend.workflow.config.records.TransitionKey;

@Configuration
public class StateMachineConfig {

    @Bean
    public Map<TransitionKey, TransitionDefinition> stateMachineTransitions() {
        return Map.of(

            // I'm using a key value pair method so that The engine does a single
            // map.get(new TransitionKey(event, app.getStatus())) achieving O(1) lookup, no iteration when trying to fin the transition.
            new TransitionKey(APPLY, NEW),
            new TransitionDefinition(
                // Triggered event
                APPLY,
                // Current state
                NEW,
                // Final state
                SUBMITTED,
                // Final processing level(this will dectate who can see this application in thier dashboard)
                LEVEL_1,
                // Breaking actions: this is the list of things that should be successful. if any fails, the entire transitions should fail and revert everything
                List.of(
                    of(INVALID_STATE_TRANSITION_VALIDATION)
                ),
                // Non-breaking actions: these are executed in the backgound, if they fail, they can be logged but not fail the entire transition
                List.of(
                    of(NOTIFICATION)
                )
            ),

            new TransitionKey(APPROVE, SUBMITTED),
            new TransitionDefinition(
                APPROVE, SUBMITTED, PENDING_FINAL_APPROVAL, LEVEL_2,
                List.of(
                    of(INVALID_STATE_TRANSITION_VALIDATION),
                    of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_1)),
                    of(RECORD_OFFICER_REVIEW, new RecordOfficerReviewArgs(LEVEL1_OFFICER_ID, LEVEL1_OFFICER_COMMENT))
                ),
                List.of(
                    of(NOTIFICATION)
                )
            ),

            new TransitionKey(REJECT, SUBMITTED),
            new TransitionDefinition(
                REJECT, SUBMITTED, REJECTED, LEVEL_1,
                List.of(
                    of(INVALID_STATE_TRANSITION_VALIDATION),
                    of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_1)),
                    of(RECORD_OFFICER_REVIEW, new RecordOfficerReviewArgs(LEVEL1_OFFICER_ID, LEVEL1_OFFICER_COMMENT))
                ),
                List.of(
                    of(NOTIFICATION)
                )
            ),

            new TransitionKey(APPROVE, PENDING_FINAL_APPROVAL),
            new TransitionDefinition(
                APPROVE, PENDING_FINAL_APPROVAL, APPROVED, LEVEL_2,
                List.of(
                    of(INVALID_STATE_TRANSITION_VALIDATION),
                    of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_2)),
                    of(REVIEWER_CONFLICT_CHECK),
                    of(RECORD_OFFICER_REVIEW, new RecordOfficerReviewArgs(LEVEL2_OFFICER_ID, LEVEL2_OFFICER_COMMENT))
                ),
                List.of(
                    of(NOTIFICATION)
                )
            ),

            new TransitionKey(REJECT, PENDING_FINAL_APPROVAL),
            new TransitionDefinition(
                REJECT, PENDING_FINAL_APPROVAL, REJECTED, null,
                List.of(
                    of(INVALID_STATE_TRANSITION_VALIDATION),
                    of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_2)),
                    of(REVIEWER_CONFLICT_CHECK),
                    of(RECORD_OFFICER_REVIEW, new RecordOfficerReviewArgs(LEVEL2_OFFICER_ID, LEVEL2_OFFICER_COMMENT))
                ),
                List.of(
                    of(NOTIFICATION)
                )
            )

        );
    }
}
