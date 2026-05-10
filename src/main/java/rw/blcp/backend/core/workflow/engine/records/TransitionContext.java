package rw.blcp.backend.core.workflow.engine.records;

import java.util.List;
import rw.blcp.backend.core.application.entity.Application;
import rw.blcp.backend.core.auth.entity.User;

public record TransitionContext(
    Application application, User actor, String comment, List<AttachmentUpload> attachments) {}
