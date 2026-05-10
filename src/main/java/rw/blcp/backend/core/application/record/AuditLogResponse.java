package rw.blcp.backend.core.application.record;

import java.time.Instant;
import java.util.UUID;
import rw.blcp.backend.core.application.enums.EApplicationEvent;
import rw.blcp.backend.core.application.enums.EApplicationStatus;

public record AuditLogResponse(
    UUID id,
    String applicationNumber,
    EApplicationEvent event,
    EApplicationStatus initialState,
    EApplicationStatus newState,
    String actorEmail,
    String comment,
    Instant createdAt) {}
