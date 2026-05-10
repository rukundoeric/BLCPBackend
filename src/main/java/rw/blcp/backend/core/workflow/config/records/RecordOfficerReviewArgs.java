package rw.blcp.backend.core.workflow.config.records;

import rw.blcp.backend.core.application.enums.EPreferenceKey;

public record RecordOfficerReviewArgs(EPreferenceKey officerIdKey, EPreferenceKey commentKey) {}
