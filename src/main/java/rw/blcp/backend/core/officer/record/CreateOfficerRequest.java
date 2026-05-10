package rw.blcp.backend.core.officer.record;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;

public record CreateOfficerRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @NotBlank @Size(min = 8) String password,
    @NotNull RoleName roleName,
    @NotNull EOfficerLevel officerLevel) {}
