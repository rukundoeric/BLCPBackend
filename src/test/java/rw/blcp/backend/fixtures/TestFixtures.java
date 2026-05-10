package rw.blcp.backend.fixtures;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.application.enums.EApplicationStatus;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.auth.entity.Role;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;

public class TestFixtures {

  public static User userWithRoles(RoleName... roleNames) {
    User user = new User();
    user.setEmail("test@example.com");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setPassword("hashed");
    Arrays.stream(roleNames).map(TestFixtures::roleWith).forEach(user.getRoles()::add);
    return user;
  }

  public static Role roleWith(RoleName name) {
    Role role = new Role();
    role.setName(name);
    return role;
  }

  public static Application submittedApplication() {
    Application app = new Application();
    app.setApplicationNumber("APP-2026-0001");
    app.setApplicantEmail("jane@example.com");
    app.setApplicantFirstName("Jane");
    app.setApplicantLastName("Doe");
    app.setBankName("Sunrise Bank");
    app.setBankType("COMMERCIAL");
    app.setStatus(EApplicationStatus.SUBMITTED);
    app.setProcessingLevel(EOfficerLevel.LEVEL_1);
    return app;
  }

  public static Application newApplication() {
    Application app = new Application();
    app.setApplicationNumber("APP-2026-0001");
    app.setApplicantEmail("jane@example.com");
    app.setApplicantFirstName("Jane");
    app.setApplicantLastName("Doe");
    app.setBankName("Sunrise Bank");
    app.setBankType("COMMERCIAL");
    app.setStatus(EApplicationStatus.NEW);
    app.setProcessingLevel(EOfficerLevel.LEVEL_1);
    return app;
  }

  public static Authentication authenticationFor(User user) {
    var authorities =
        user.getRoles().stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
            .collect(Collectors.toList());
    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }
}
