package rw.blcp.backend.auth.dto;

import java.util.Set;
import rw.blcp.backend.auth.RoleName;

public record UserProfileResponse(
    String email, String firstName, String lastName, Set<RoleName> roles) {}
