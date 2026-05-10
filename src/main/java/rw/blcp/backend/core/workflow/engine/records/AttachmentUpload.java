package rw.blcp.backend.core.workflow.engine.records;

import org.springframework.web.multipart.MultipartFile;
import rw.blcp.backend.core.application.enums.EDocumentType;

public record AttachmentUpload(MultipartFile file, EDocumentType documentType) {}
