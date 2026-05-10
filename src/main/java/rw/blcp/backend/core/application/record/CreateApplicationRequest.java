package rw.blcp.backend.core.application.record;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import rw.blcp.backend.core.application.enums.EDocumentType;

public record CreateApplicationRequest(
    @NotBlank(message = "Applicant email is required")
        @Email(message = "Applicant email must be a valid email address")
        String applicantEmail,
    @NotBlank(message = "Applicant first name is required") @Size(max = 100)
        String applicantFirstName,
    @NotBlank(message = "Applicant last name is required") @Size(max = 100)
        String applicantLastName,
    @NotBlank(message = "Bank name is required") @Size(max = 200) String bankName,
    @NotBlank(message = "Bank type is required") @Size(max = 100) String bankType,
    @Size(max = 2000) String notes,
    List<EDocumentType> documentTypes) {}
