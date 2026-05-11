package rw.blcp.backend.core.application.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.application.enums.EApplicationEvent;
import rw.blcp.backend.core.application.enums.EDocumentType;
import rw.blcp.backend.core.application.record.ApplicationDetailResponse;
import rw.blcp.backend.core.application.record.ApplicationFilter;
import rw.blcp.backend.core.application.record.ApplicationResponse;
import rw.blcp.backend.core.application.record.AttachmentResponse;
import rw.blcp.backend.core.application.record.CreateApplicationRequest;
import rw.blcp.backend.core.application.record.TakeActionRequest;
import rw.blcp.backend.core.application.repository.ApplicationAttachmentRepository;
import rw.blcp.backend.core.application.repository.ApplicationRepository;
import rw.blcp.backend.core.application.repository.ApplicationSpecifications;
import rw.blcp.backend.core.application.repository.AuditLogRepository;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.workflow.engine.StateMachineEngine;
import rw.blcp.backend.core.workflow.engine.records.AttachmentUpload;
import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final ApplicationAttachmentRepository applicationAttachmentRepository;
  private final AuditLogRepository auditLogRepository;
  private final StateMachineEngine stateMachineEngine;

  @Transactional
  public ApplicationResponse create(
      CreateApplicationRequest request, List<MultipartFile> files, User actor) {
    Application application = new Application();
    application.setApplicationNumber(generateApplicationNumber());
    application.setApplicant(actor);
    application.setApplicantEmail(actor.getEmail());
    application.setApplicantFirstName(actor.getFirstName());
    application.setApplicantLastName(actor.getLastName());
    application.setBankName(request.bankName());
    application.setBankType(request.bankType());
    application.setNotes(request.notes());

    applicationRepository.save(application);

    stateMachineEngine.execute(
        EApplicationEvent.APPLY,
        new TransitionContext(
            application, actor, null, buildUploads(files, request.documentTypes())));

    log.info(
        "Application {} created and submitted by {}",
        application.getApplicationNumber(),
        actor.getEmail());

    return toResponse(application);
  }

  @Transactional
  public ApplicationResponse resubmit(
      String applicationNumber, List<MultipartFile> files, User actor) {
    Application application =
        applicationRepository
            .findByApplicationNumber(applicationNumber)
            .orElseThrow(() -> new ApiException(ErrorCode.APPLICATION_NOT_FOUND));

    stateMachineEngine.execute(
        EApplicationEvent.RESUBMIT,
        new TransitionContext(application, actor, null, buildUploads(files, List.of())));

    log.info("Application {} resubmitted by {}", applicationNumber, actor.getEmail());

    return toResponse(application);
  }

  @Transactional
  public ApplicationResponse takeAction(
      String applicationNumber, TakeActionRequest request, User actor) {
    if ((request.event() == EApplicationEvent.REJECT
            || request.event() == EApplicationEvent.REQUEST_FOR_ACTION)
        && (request.comment() == null || request.comment().isBlank())) {
      throw new ApiException(ErrorCode.VALIDATION_FAILED, "Comment is required for this action");
    }

    Application application =
        applicationRepository
            .findByApplicationNumber(applicationNumber)
            .orElseThrow(() -> new ApiException(ErrorCode.APPLICATION_NOT_FOUND));

    stateMachineEngine.execute(
        request.event(), new TransitionContext(application, actor, request.comment(), null));

    log.info(
        "Action {} taken on application {} by {}",
        request.event(),
        applicationNumber,
        actor.getEmail());

    return toResponse(application);
  }

  @Transactional(readOnly = true)
  public Page<ApplicationResponse> fetchApplications(
      ApplicationFilter filter, Pageable pageable, User actor) {
    return applicationRepository
        .findAll(ApplicationSpecifications.withFilter(filter, actor), pageable)
        .map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public ApplicationDetailResponse getDetail(String applicationNumber, User actor) {
    Application application =
        applicationRepository
            .findOne(
                ApplicationSpecifications.withFilter(
                    new ApplicationFilter(applicationNumber, null, null, null, null), actor))
            .orElseThrow(() -> new ApiException(ErrorCode.APPLICATION_NOT_FOUND));

    List<AttachmentResponse> attachments =
        applicationAttachmentRepository.findByApplication(application).stream()
            .map(
                applicationAttachment ->
                    new AttachmentResponse(
                        applicationAttachment.getId(),
                        applicationAttachment.getAttachment().getFilename(),
                        applicationAttachment.getAttachment().getMimeType(),
                        applicationAttachment.getAttachment().getFileSize(),
                        applicationAttachment.getDocumentType(),
                        applicationAttachment.getSubmissionVersion()))
            .toList();

    String latestOfficerComment =
        auditLogRepository
            .findFirstByApplicationAndCommentIsNotNullOrderByCreatedAtDesc(application)
            .map(log -> log.getComment())
            .orElse(null);

    return new ApplicationDetailResponse(
        application.getId(),
        application.getApplicationNumber(),
        application.getStatus(),
        application.getProcessingLevel(),
        application.getBankName(),
        application.getBankType(),
        application.getNotes(),
        application.getApplicantEmail(),
        application.getApplicantFirstName(),
        application.getApplicantLastName(),
        application.getCreatedAt(),
        application.getUpdatedAt(),
        attachments,
        latestOfficerComment);
  }

  private List<AttachmentUpload> buildUploads(
      List<MultipartFile> files, List<EDocumentType> documentTypes) {
    if (files == null || files.isEmpty()) return Collections.emptyList();
    List<EDocumentType> types =
        (documentTypes != null && !documentTypes.isEmpty())
            ? documentTypes
            : Collections.emptyList();
    List<AttachmentUpload> uploads = new java.util.ArrayList<>();
    for (int i = 0; i < files.size(); i++) {
      EDocumentType type = i < types.size() ? types.get(i) : EDocumentType.OTHER;
      uploads.add(new AttachmentUpload(files.get(i), type));
    }
    return uploads;
  }

  private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final SecureRandom RANDOM = new SecureRandom();

  private String generateApplicationNumber() {
    LocalDateTime now = LocalDateTime.now();
    String timestamp =
        String.format(
            "B%02d%02d%02d%02d%02d%02d",
            now.getYear() % 100,
            now.getMonthValue(),
            now.getDayOfMonth(),
            now.getHour(),
            now.getMinute(),
            now.getSecond());
    StringBuilder suffix = new StringBuilder(4);
    for (int i = 0; i < 4; i++) {
      suffix.append(RANDOM_CHARS.charAt(RANDOM.nextInt(RANDOM_CHARS.length())));
    }
    return timestamp + suffix;
  }

  private ApplicationResponse toResponse(Application app) {
    return new ApplicationResponse(
        app.getId(),
        app.getApplicationNumber(),
        app.getStatus(),
        app.getProcessingLevel(),
        app.getBankName(),
        app.getBankType(),
        app.getNotes(),
        app.getApplicantEmail(),
        app.getApplicantFirstName(),
        app.getApplicantLastName(),
        app.getCreatedAt(),
        app.getUpdatedAt());
  }
}
