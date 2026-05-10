package rw.blcp.backend.core.application.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;
import rw.blcp.backend.core.application.enums.EApplicationStatus;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplicationResponse(
    UUID id,
    String applicationNumber,
    EApplicationStatus status,
    EOfficerLevel processingLevel,
    String bankName,
    String bankType,
    String notes,
    String applicantEmail,
    String applicantFirstName,
    String applicantLastName,
    Instant createdAt,
    Instant updatedAt) {}
