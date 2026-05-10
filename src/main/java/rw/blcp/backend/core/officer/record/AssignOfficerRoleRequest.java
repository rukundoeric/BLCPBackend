package rw.blcp.backend.core.officer.record;

import jakarta.validation.constraints.NotNull;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;

public record AssignOfficerRoleRequest(
    @NotNull RoleName roleName, @NotNull EOfficerLevel officerLevel) {}
