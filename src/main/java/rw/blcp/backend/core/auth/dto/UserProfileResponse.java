package rw.blcp.backend.core.auth.dto;

import java.util.Set;
import rw.blcp.backend.core.auth.RoleName;

public record UserProfileResponse(
    String email, String firstName, String lastName, Set<RoleName> roles) {}
