package rw.blcp.backend.core.workflow.config;

import static rw.blcp.backend.core.application.enums.EApplicationEvent.APPLY;
import static rw.blcp.backend.core.application.enums.EApplicationEvent.APPROVE;
import static rw.blcp.backend.core.application.enums.EApplicationEvent.REJECT;
import static rw.blcp.backend.core.application.enums.EApplicationEvent.REQUEST_FOR_ACTION;
import static rw.blcp.backend.core.application.enums.EApplicationEvent.RESUBMIT;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.APPROVED;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.NEW;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.PENDING_FINAL_APPROVAL;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.PENDING_RESUBMISSION;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.REJECTED;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.RESUBMITTED;
import static rw.blcp.backend.core.application.enums.EApplicationStatus.SUBMITTED;
import static rw.blcp.backend.core.application.enums.EPreferenceKey.LEVEL1_OFFICER_COMMENT;
import static rw.blcp.backend.core.application.enums.EPreferenceKey.LEVEL1_OFFICER_ID;
import static rw.blcp.backend.core.application.enums.EPreferenceKey.LEVEL2_OFFICER_COMMENT;
import static rw.blcp.backend.core.application.enums.EPreferenceKey.LEVEL2_OFFICER_ID;
import static rw.blcp.backend.core.application.enums.EPreferenceKey.REQUEST_FOR_ACTION_OFFICER_COMMENT;
import static rw.blcp.backend.core.application.enums.EPreferenceKey.REQUEST_FOR_ACTION_OFFICER_ID;
import static rw.blcp.backend.core.officer.enums.EOfficerLevel.LEVEL_1;
import static rw.blcp.backend.core.officer.enums.EOfficerLevel.LEVEL_2;
import static rw.blcp.backend.core.workflow.config.records.ActionDefinition.of;
import static rw.blcp.backend.core.workflow.enums.EActionType.APPLICANT_OWNERSHIP_VALIDATION;
import static rw.blcp.backend.core.workflow.enums.EActionType.DUPLICATE_REVIEWER_VALIDATION;
import static rw.blcp.backend.core.workflow.enums.EActionType.NOTIFICATION;
import static rw.blcp.backend.core.workflow.enums.EActionType.OFFICER_LEVEL_VALIDATION;
import static rw.blcp.backend.core.workflow.enums.EActionType.RECORD_OFFICER_REVIEW;
import static rw.blcp.backend.core.workflow.enums.EActionType.RESUBMIT_DOCUMENTS;
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
    return Map.ofEntries(

        /* APPLY: NEW -> SUBMITTED */
        Map.entry(
            new TransitionKey(APPLY, NEW),
            new TransitionDefinition(
                APPLY,
                NEW,
                SUBMITTED,
                LEVEL_1,
                List.of(of(SET_APPLICATION_ATTACHMENTS)),
                List.of(of(NOTIFICATION)))),

        /* APPROVE: SUBMITTED -> PENDING_FINAL_APPROVAL  Requires: LEVEL_1 OFFICER */
        Map.entry(
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
                List.of(of(NOTIFICATION)))),

        /* REJECT: SUBMITTED -> REJECTED  Requires: LEVEL_1 OFFICER */
        Map.entry(
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
                List.of(of(NOTIFICATION)))),

        /* REQUEST_FOR_ACTION: SUBMITTED -> PENDING_RESUBMISSION  Requires: LEVEL_1 OFFICER */
        Map.entry(
            new TransitionKey(REQUEST_FOR_ACTION, SUBMITTED),
            new TransitionDefinition(
                REQUEST_FOR_ACTION,
                SUBMITTED,
                PENDING_RESUBMISSION,
                LEVEL_1,
                List.of(
                    of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_1)),
                    of(
                        RECORD_OFFICER_REVIEW,
                        new RecordOfficerReviewArgs(
                            REQUEST_FOR_ACTION_OFFICER_ID, REQUEST_FOR_ACTION_OFFICER_COMMENT))),
                List.of(of(NOTIFICATION)))),

        /* APPROVE: PENDING_FINAL_APPROVAL -> APPROVED  Requires: LEVEL_2 OFFICER */
        Map.entry(
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
                List.of(of(NOTIFICATION)))),

        /* REJECT: PENDING_FINAL_APPROVAL -> REJECTED  Requires: LEVEL_2 OFFICER */
        Map.entry(
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
                List.of(of(NOTIFICATION)))),

        /* REQUEST_FOR_ACTION: PENDING_FINAL_APPROVAL -> PENDING_RESUBMISSION  Requires: LEVEL_2 OFFICER */
        Map.entry(
            new TransitionKey(REQUEST_FOR_ACTION, PENDING_FINAL_APPROVAL),
            new TransitionDefinition(
                REQUEST_FOR_ACTION,
                PENDING_FINAL_APPROVAL,
                PENDING_RESUBMISSION,
                LEVEL_2,
                List.of(
                    of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_2)),
                    of(DUPLICATE_REVIEWER_VALIDATION),
                    of(
                        RECORD_OFFICER_REVIEW,
                        new RecordOfficerReviewArgs(
                            REQUEST_FOR_ACTION_OFFICER_ID, REQUEST_FOR_ACTION_OFFICER_COMMENT))),
                List.of(of(NOTIFICATION)))),

        /* RESUBMIT: PENDING_RESUBMISSION -> RESUBMITTED  Requires: APPLICANT (owner) */
        Map.entry(
            new TransitionKey(RESUBMIT, PENDING_RESUBMISSION),
            new TransitionDefinition(
                RESUBMIT,
                PENDING_RESUBMISSION,
                RESUBMITTED,
                LEVEL_1,
                List.of(of(APPLICANT_OWNERSHIP_VALIDATION), of(RESUBMIT_DOCUMENTS)),
                List.of(of(NOTIFICATION)))),

        /* APPROVE: RESUBMITTED -> PENDING_FINAL_APPROVAL  Requires: LEVEL_1 OFFICER */
        Map.entry(
            new TransitionKey(APPROVE, RESUBMITTED),
            new TransitionDefinition(
                APPROVE,
                RESUBMITTED,
                PENDING_FINAL_APPROVAL,
                LEVEL_2,
                List.of(
                    of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_1)),
                    of(
                        RECORD_OFFICER_REVIEW,
                        new RecordOfficerReviewArgs(LEVEL1_OFFICER_ID, LEVEL1_OFFICER_COMMENT))),
                List.of(of(NOTIFICATION)))),

        /* REJECT: RESUBMITTED -> REJECTED  Requires: LEVEL_1 OFFICER */
        Map.entry(
            new TransitionKey(REJECT, RESUBMITTED),
            new TransitionDefinition(
                REJECT,
                RESUBMITTED,
                REJECTED,
                null,
                List.of(
                    of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_1)),
                    of(
                        RECORD_OFFICER_REVIEW,
                        new RecordOfficerReviewArgs(LEVEL1_OFFICER_ID, LEVEL1_OFFICER_COMMENT))),
                List.of(of(NOTIFICATION)))),

        /* REQUEST_FOR_ACTION: RESUBMITTED -> PENDING_RESUBMISSION  Requires: LEVEL_1 OFFICER */
        Map.entry(
            new TransitionKey(REQUEST_FOR_ACTION, RESUBMITTED),
            new TransitionDefinition(
                REQUEST_FOR_ACTION,
                RESUBMITTED,
                PENDING_RESUBMISSION,
                LEVEL_1,
                List.of(
                    of(OFFICER_LEVEL_VALIDATION, new OfficerLevelValidationArgs(LEVEL_1)),
                    of(
                        RECORD_OFFICER_REVIEW,
                        new RecordOfficerReviewArgs(
                            REQUEST_FOR_ACTION_OFFICER_ID, REQUEST_FOR_ACTION_OFFICER_COMMENT))),
                List.of(of(NOTIFICATION)))));
  }
}
