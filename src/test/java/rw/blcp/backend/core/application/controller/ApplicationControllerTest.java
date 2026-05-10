package rw.blcp.backend.core.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import rw.blcp.backend.common.handler.GlobalExceptionHandler;
import rw.blcp.backend.core.application.enums.EApplicationEvent;
import rw.blcp.backend.core.application.enums.EApplicationStatus;
import rw.blcp.backend.core.application.record.ApplicationResponse;
import rw.blcp.backend.core.application.record.CreateApplicationRequest;
import rw.blcp.backend.core.application.record.TakeActionRequest;
import rw.blcp.backend.core.application.service.ApplicationService;
import rw.blcp.backend.core.auth.RoleName;
import rw.blcp.backend.core.auth.aspect.AuthorizationAspect;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.fixtures.TestFixtures;

@WebMvcTest(
    value = ApplicationController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import({GlobalExceptionHandler.class, AuthorizationAspect.class})
class ApplicationControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean ApplicationService applicationService;

  @Test
  void createApplication_withValidRequest_returns201() throws Exception {
    when(applicationService.create(any(), any(), any())).thenReturn(stubResponse());

    mockMvc
        .perform(multipart("/api/v1/public/applications").file(validDataPart()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.applicationNumber").value("APP-2026-0001"));
  }

  @Test
  void createApplication_withMissingBankName_returns400() throws Exception {
    CreateApplicationRequest invalid =
        new CreateApplicationRequest(
            "jane@example.com", "Jane", "Doe", null, "COMMERCIAL", null, null);
    MockMultipartFile part =
        new MockMultipartFile(
            "data",
            "data",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(invalid));

    mockMvc
        .perform(multipart("/api/v1/public/applications").file(part))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.errorCode").value("VALIDATION_FAILED"));
  }

  @Test
  void createApplication_withInvalidEmail_returns400() throws Exception {
    CreateApplicationRequest invalid =
        new CreateApplicationRequest(
            "not-an-email", "Jane", "Doe", "Sunrise Bank", "COMMERCIAL", null, null);
    MockMultipartFile part =
        new MockMultipartFile(
            "data",
            "data",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(invalid));

    mockMvc
        .perform(multipart("/api/v1/public/applications").file(part))
        .andExpect(status().isBadRequest());
  }

  @Test
  void fetchApplications_asOfficer_returns200() throws Exception {
    var officer = TestFixtures.userWithRoles(RoleName.OFFICER);
    when(applicationService.fetchApplications(any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(stubResponse())));

    mockMvc
        .perform(
            get("/api/v1/applications")
                .with(authentication(TestFixtures.authenticationFor(officer))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isArray());
  }

  @Test
  void fetchApplications_withoutAuthentication_returns403() throws Exception {
    mockMvc.perform(get("/api/v1/applications")).andExpect(status().isForbidden());
  }

  @Test
  void fetchApplications_asApplicant_returns403() throws Exception {
    var applicant = TestFixtures.userWithRoles(RoleName.APPLICANT);

    mockMvc
        .perform(
            get("/api/v1/applications")
                .with(authentication(TestFixtures.authenticationFor(applicant))))
        .andExpect(status().isForbidden());
  }

  @Test
  void takeAction_asOfficer_withValidRequest_returns200() throws Exception {
    var officer = TestFixtures.userWithRoles(RoleName.OFFICER);
    when(applicationService.takeAction(any(), any(), any())).thenReturn(stubResponse());

    mockMvc
        .perform(
            post("/api/v1/applications/APP-2026-0001/action")
                .with(authentication(TestFixtures.authenticationFor(officer)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new TakeActionRequest(EApplicationEvent.APPROVE, "Looks good"))))
        .andExpect(status().isOk());
  }

  @Test
  void takeAction_whenServiceThrowsInvalidTransition_returns422() throws Exception {
    var officer = TestFixtures.userWithRoles(RoleName.OFFICER);
    when(applicationService.takeAction(any(), any(), any()))
        .thenThrow(new ApiException(ErrorCode.INVALID_STATE_TRANSITION));

    mockMvc
        .perform(
            post("/api/v1/applications/APP-2026-0001/action")
                .with(authentication(TestFixtures.authenticationFor(officer)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new TakeActionRequest(EApplicationEvent.APPROVE, null))))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.error.errorCode").value("INVALID_STATE_TRANSITION"));
  }

  private MockMultipartFile validDataPart() throws Exception {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            "jane@example.com", "Jane", "Doe", "Sunrise Bank", "COMMERCIAL", "Notes", null);
    return new MockMultipartFile(
        "data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));
  }

  private ApplicationResponse stubResponse() {
    return new ApplicationResponse(
        UUID.randomUUID(),
        "APP-2026-0001",
        EApplicationStatus.SUBMITTED,
        EOfficerLevel.LEVEL_1,
        "Sunrise Bank",
        "COMMERCIAL",
        null,
        "jane@example.com",
        "Jane",
        "Doe",
        Instant.now(),
        Instant.now());
  }
}
