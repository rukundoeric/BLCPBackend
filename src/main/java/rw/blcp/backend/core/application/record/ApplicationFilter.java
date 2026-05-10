package rw.blcp.backend.core.application.record;

import rw.blcp.backend.core.application.enums.EApplicationStatus;

public record ApplicationFilter(
    String applicationNumber,
    String bankName,
    String bankType,
    EApplicationStatus status,
    String applicantEmail) {}
