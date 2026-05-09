package rw.blcp.backend.workflow.config;

import rw.blcp.backend.workflow.enums.EPreferenceKey;

public record RecordOfficerReviewArgs(EPreferenceKey officerIdKey, EPreferenceKey commentKey) {}
