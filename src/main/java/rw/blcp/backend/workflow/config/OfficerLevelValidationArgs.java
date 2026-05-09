package rw.blcp.backend.workflow.config;

import rw.blcp.backend.workflow.enums.EOfficerLevel;

public record OfficerLevelValidationArgs(EOfficerLevel requiredLevel) {}
