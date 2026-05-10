package rw.blcp.backend.core.application.service;

import java.time.Year;
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
import rw.blcp.backend.core.application.record.ApplicationFilter;
import rw.blcp.backend.core.application.record.ApplicationResponse;
import rw.blcp.backend.core.application.record.CreateApplicationRequest;
import rw.blcp.backend.core.application.record.TakeActionRequest;
import rw.blcp.backend.core.application.repository.ApplicationRepository;
import rw.blcp.backend.core.application.repository.ApplicationSpecifications;
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

  private String generateApplicationNumber() {
    long count = applicationRepository.count() + 1;
    return String.format("APP-%d-%04d", Year.now().getValue(), count);
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
