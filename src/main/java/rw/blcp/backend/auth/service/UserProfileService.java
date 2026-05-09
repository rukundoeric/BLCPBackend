package rw.blcp.backend.auth.service;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import rw.blcp.backend.auth.RoleName;
import rw.blcp.backend.auth.dto.UserProfileResponse;
import rw.blcp.backend.auth.entity.Role;
import rw.blcp.backend.auth.entity.User;

@Service
public class UserProfileService {

  public UserProfileResponse getProfile(User user) {
    Set<RoleName> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

    return new UserProfileResponse(user.getEmail(), user.getFirstName(), user.getLastName(), roles);
  }
}
