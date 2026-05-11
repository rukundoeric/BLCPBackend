package rw.blcp.backend.core.application.record;

import java.util.UUID;
import rw.blcp.backend.core.application.enums.EDocumentType;

public record AttachmentResponse(
    UUID id,
    String filename,
    String mimeType,
    Long fileSize,
    EDocumentType documentType,
    int submissionVersion) {}
