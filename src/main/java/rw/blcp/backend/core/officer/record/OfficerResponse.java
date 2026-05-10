package rw.blcp.backend.core.officer.record;

import java.util.UUID;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;

public record OfficerResponse(
    UUID officerId,
    UUID userId,
    String email,
    String firstName,
    String lastName,
    EOfficerLevel officerLevel) {}
