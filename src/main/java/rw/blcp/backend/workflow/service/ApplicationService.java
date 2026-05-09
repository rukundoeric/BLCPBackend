package rw.blcp.backend.workflow.service;

import java.time.Year;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;
import rw.blcp.backend.workflow.dto.ApplicationFilter;
import rw.blcp.backend.workflow.dto.ApplicationResponse;
import rw.blcp.backend.workflow.dto.CreateApplicationRequest;
import rw.blcp.backend.workflow.dto.TakeActionRequest;
import rw.blcp.backend.workflow.engine.StateMachineEngine;
import rw.blcp.backend.workflow.engine.records.TransitionContext;
import rw.blcp.backend.workflow.entity.Application;
import rw.blcp.backend.workflow.enums.EApplicationEvent;
import rw.blcp.backend.workflow.repository.ApplicationRepository;
import rw.blcp.backend.workflow.repository.ApplicationSpecifications;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StateMachineEngine stateMachineEngine;

    @Transactional
    public ApplicationResponse create(CreateApplicationRequest request, User actor) {
        Application application = new Application();
        application.setApplicationNumber(generateApplicationNumber());
        application.setApplicant(actor);
        application.setApplicantEmail(actor != null ? actor.getEmail() : request.applicantEmail());
        application.setApplicantFirstName(
                actor != null ? actor.getFirstName() : request.applicantFirstName());
        application.setApplicantLastName(
                actor != null ? actor.getLastName() : request.applicantLastName());
        application.setBankName(request.bankName());
        application.setBankType(request.bankType());
        application.setNotes(request.notes());

        applicationRepository.save(application);

        stateMachineEngine.execute(
                EApplicationEvent.APPLY, new TransitionContext(application, actor, null));

        log.info(
                "Application {} created and submitted by {}",
                application.getApplicationNumber(),
                actor != null ? actor.getEmail() : "guest");

        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse takeAction(
            String applicationNumber, TakeActionRequest request, User actor) {
        if (request.event() == EApplicationEvent.REJECT
                && (request.comment() == null || request.comment().isBlank())) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "Comment is required when rejecting an application");
        }

        Application application =
                applicationRepository
                        .findByApplicationNumber(applicationNumber)
                        .orElseThrow(() -> new ApiException(ErrorCode.APPLICATION_NOT_FOUND));

        stateMachineEngine.execute(
                request.event(), new TransitionContext(application, actor, request.comment()));

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
