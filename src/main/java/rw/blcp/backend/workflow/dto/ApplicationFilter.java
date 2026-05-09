package rw.blcp.backend.workflow.dto;

import rw.blcp.backend.workflow.enums.EApplicationStatus;

public record ApplicationFilter(
        String applicationNumber,
        String bankName,
        String bankType,
        EApplicationStatus status,
        String applicantEmail) {}
