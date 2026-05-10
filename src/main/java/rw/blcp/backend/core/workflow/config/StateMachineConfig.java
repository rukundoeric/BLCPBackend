package rw.blcp.backend.core.workflow.config;

import static rw.blcp.backend.core.application.enums.EApplicationEvent.APPLY;
import static rw.blcp.backend.core.application.enums.EApplicationEvent.APPROVE;
import static rw.blcp.backend.core.application.enums.EApplicationEvent.REJECT;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.APPROVED;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.NEW;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.PENDING_FINAL_APPROVAL;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.REJECTED;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.SUBMITTED;
import static rw.blcp.backend.core.application.enums.EPreferenceKey.LEVEL1_OFFICER_COMMENT;
import static rw.blcp.backend.core.application.enums.EPreferenceKey.LEVEL1_OFFICER_ID;
import static rw.blcp.backend.core.application.enums.EPreferenceKey.LEVEL2_OFFICER_COMMENT;
import static rw.blcp.backend.core.application.enums.EPreferenceKey.LEVEL2_OFFICER_ID;
import static rw.blcp.backend.core.officer.enums.EOfficerLevel.LEVEL_1;
import static rw.blcp.backend.core.officer.enums.EOfficerLevel.LEVEL_2;
import static rw.blcp.backend.core.workflow.config.records.ActionDefinition.of;
import static rw.blcp.backend.core.workflow.enums.EActionType.DUPLICATE_REVIEWER_VALIDATION;
import static rw.blcp.backend.core.workflow.enums.EActionType.NOTIFICATION;
import static rw.blcp.backend.core.workflow.enums.EActionType.OFFICER_LEVEL_VALIDATION;
import static rw.blcp.backend.core.workflow.enums.EActionType.RECORD_OFFICER_REVIEW;
import static rw.blcp.backend.core.workflow.enums.EActionType.SET_APPLICATION_ATTACHMENTS;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rw.blcp.backend.core.workflow.config.records.OfficerLevelValidationArgs;
import rw.blcp.backend.core.workflow.config.records.RecordOfficerReviewArgs;
import rw.blcp.backend.core.workflow.config.records.TransitionDefinition;
import rw.blcp.backend.core.workflow.config.records.TransitionKey;

@Configuration
public class StateMachineConfig {

  @Bean
  public Map<TransitionKey, TransitionDefinition> stateMachineTransitions() {
    return Map.of(

        // I'm using a key value pair method so that The engine does a single
        // map.get(new TransitionKey(event, app.getStatus())) achieving O(1) lookup, no
        // iteration when trying to find the transition.

        /* APPLY: NEW -> SUBMITTED  */
        new TransitionKey(APPLY, NEW),
        new TransitionDefinition(
            // Triggered event
            APPLY,
            // Current state
            NEW,
            // Final state
            SUBMITTED,
            // Final processing level(this will dectate who can see this application in
            // their dashboard)
            LEVEL_1,
            // Breaking actions: this is the list of things that should be successful.
            // if any fails, the entire transitions should fail and revert everything
            List.of(of(SET_APPLICATION_ATTACHMENTS)),
            // Non-breaking actions: these are executed in the background, if they fail,
            // they can be logged but not fail the entire transition
            List.of(of(NOTIFICATION))),
        /* APPROVE: SUBMITTED -> PENDING_FINAL_APPROVAL  Requires: LEVEL_1 OFFICER*/
        new TransitionKey(APPROVE, SUBMITTED),
        new TransitionDefinition(
            APPROVE,
            SUBMITTED,
            PENDING_FINAL_APPROVAL,
            LEVEL_2,
            List.of(
                of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_1)),
                of(
                    RECORD_OFFICER_REVIEW,
                    new RecordOfficerReviewArgs(LEVEL1_OFFICER_ID, LEVEL1_OFFICER_COMMENT))),
            List.of(of(NOTIFICATION))),
        /* REJECT: SUBMITTED -> REJECTED  Requires: LEVEL_1 OFFICER*/
        new TransitionKey(REJECT, SUBMITTED),
        new TransitionDefinition(
            REJECT,
            SUBMITTED,
            REJECTED,
            LEVEL_1,
            List.of(
                of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_1)),
                of(
                    RECORD_OFFICER_REVIEW,
                    new RecordOfficerReviewArgs(LEVEL1_OFFICER_ID, LEVEL1_OFFICER_COMMENT))),
            List.of(of(NOTIFICATION))),
        /* APPROVE: PENDING_FINAL_APPROVAL -> APPROVED  Requires: LEVEL_2 OFFICER*/
        new TransitionKey(APPROVE, PENDING_FINAL_APPROVAL),
        new TransitionDefinition(
            APPROVE,
            PENDING_FINAL_APPROVAL,
            APPROVED,
            LEVEL_2,
            List.of(
                of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_2)),
                of(DUPLICATE_REVIEWER_VALIDATION),
                of(
                    RECORD_OFFICER_REVIEW,
                    new RecordOfficerReviewArgs(LEVEL2_OFFICER_ID, LEVEL2_OFFICER_COMMENT))),
            List.of(of(NOTIFICATION))),
        /* REJECT: PENDING_FINAL_APPROVAL -> REJECTED Requires: LEVEL_2 OFFICER */
        new TransitionKey(REJECT, PENDING_FINAL_APPROVAL),
        new TransitionDefinition(
            REJECT,
            PENDING_FINAL_APPROVAL,
            REJECTED,
            null,
            List.of(
                of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_2)),
                of(DUPLICATE_REVIEWER_VALIDATION),
                of(
                    RECORD_OFFICER_REVIEW,
                    new RecordOfficerReviewArgs(LEVEL2_OFFICER_ID, LEVEL2_OFFICER_COMMENT))),
            List.of(of(NOTIFICATION))));
  }
}
