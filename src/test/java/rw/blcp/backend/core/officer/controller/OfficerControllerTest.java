package rw.blcp.backend.core.officer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import rw.blcp.backend.common.handler.GlobalExceptionHandler;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.auth.aspect.AuthorizationAspect;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;
import rw.blcp.backend.core.officer.record.AssignOfficerRoleRequest;
import rw.blcp.backend.core.officer.record.CreateOfficerRequest;
import rw.blcp.backend.core.officer.record.OfficerResponse;
import rw.blcp.backend.core.officer.service.OfficerService;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.fixtures.TestFixtures;

@WebMvcTest(value = OfficerController.class)
@Import({GlobalExceptionHandler.class, AuthorizationAspect.class, AopAutoConfiguration.class})
class OfficerControllerTest {

  @TestConfiguration
  static class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return http.csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .build();
    }
  }

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean OfficerService officerService;

  @Test
  void createOfficer_asAdmin_returns201() throws Exception {
    var admin = TestFixtures.userWithRoles(RoleName.ADMIN);
    when(officerService.createOfficer(any())).thenReturn(stubOfficerResponse());

    mockMvc
        .perform(
            post("/api/v1/admin/officers")
                .with(authentication(TestFixtures.authenticationFor(admin)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.email").value("new.officer@nbr.rw"))
        .andExpect(jsonPath("$.data.officerLevel").value("LEVEL_1"));
  }

  @Test
  void createOfficer_asOfficer_returns403() throws Exception {
    var officer = TestFixtures.userWithRoles(RoleName.OFFICER);

    mockMvc
        .perform(
            post("/api/v1/admin/officers")
                .with(authentication(TestFixtures.authenticationFor(officer)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest())))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.errorCode").value("ACCESS_DENIED"));
  }

  @Test
  void createOfficer_withoutAuthentication_returns401() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/admin/officers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest())))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createOfficer_withBlankEmail_returns400() throws Exception {
    var admin = TestFixtures.userWithRoles(RoleName.ADMIN);
    CreateOfficerRequest invalid =
        new CreateOfficerRequest(
            "", "First", "Last", "Test@1234", RoleName.OFFICER, EOfficerLevel.LEVEL_1);

    mockMvc
        .perform(
            post("/api/v1/admin/officers")
                .with(authentication(TestFixtures.authenticationFor(admin)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.errorCode").value("VALIDATION_FAILED"));
  }

  @Test
  void createOfficer_whenEmailAlreadyExists_returns409() throws Exception {
    var admin = TestFixtures.userWithRoles(RoleName.ADMIN);
    when(officerService.createOfficer(any()))
        .thenThrow(new ApiException(ErrorCode.EMAIL_ALREADY_IN_USE));

    mockMvc
        .perform(
            post("/api/v1/admin/officers")
                .with(authentication(TestFixtures.authenticationFor(admin)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.errorCode").value("EMAIL_ALREADY_IN_USE"));
  }

  @Test
  void assignOfficerRole_asAdmin_returns200() throws Exception {
    var admin = TestFixtures.userWithRoles(RoleName.ADMIN);
    UUID userId = UUID.randomUUID();
    when(officerService.assignOfficerRole(any(), any())).thenReturn(stubOfficerResponse());

    mockMvc
        .perform(
            post("/api/v1/admin/users/{userId}/officer-role", userId)
                .with(authentication(TestFixtures.authenticationFor(admin)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AssignOfficerRoleRequest(
                            RoleName.SENIOR_OFFICER, EOfficerLevel.LEVEL_2))))
        .andExpect(status().isOk());
  }

  @Test
  void assignOfficerRole_whenAlreadyAssigned_returns409() throws Exception {
    var admin = TestFixtures.userWithRoles(RoleName.ADMIN);
    UUID userId = UUID.randomUUID();
    when(officerService.assignOfficerRole(any(), any()))
        .thenThrow(new ApiException(ErrorCode.OFFICER_ALREADY_EXISTS));

    mockMvc
        .perform(
            post("/api/v1/admin/users/{userId}/officer-role", userId)
                .with(authentication(TestFixtures.authenticationFor(admin)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AssignOfficerRoleRequest(RoleName.OFFICER, EOfficerLevel.LEVEL_1))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.errorCode").value("OFFICER_ALREADY_EXISTS"));
  }

  private CreateOfficerRequest validCreateRequest() {
    return new CreateOfficerRequest(
        "new.officer@nbr.rw",
        "Eric",
        "Mugisha",
        "Test@1234",
        RoleName.OFFICER,
        EOfficerLevel.LEVEL_1);
  }

  private OfficerResponse stubOfficerResponse() {
    return new OfficerResponse(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "new.officer@nbr.rw",
        "Eric",
        "Mugisha",
        EOfficerLevel.LEVEL_1);
  }
}
