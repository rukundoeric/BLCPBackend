package rw.blcp.backend.core.workflow.engine.actions;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rw.blcp.backend.core.application.entity.ApplicationAttachment;
import rw.blcp.backend.core.application.entity.Attachment;
import rw.blcp.backend.core.application.repository.ApplicationAttachmentRepository;
import rw.blcp.backend.core.application.repository.AttachmentRepository;
import rw.blcp.backend.core.workflow.engine.Action;
import rw.blcp.backend.core.workflow.engine.records.AttachmentUpload;
import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.core.workflow.enums.EActionType;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetApplicationAttachmentsAction implements Action<Void> {

  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

  private final AttachmentRepository attachmentRepository;
  private final ApplicationAttachmentRepository applicationAttachmentRepository;

  @Override
  public EActionType getType() {
    return EActionType.SET_APPLICATION_ATTACHMENTS;
  }

  @Override
  public void execute(TransitionContext ctx, Void args) {
    /* In a production level code, where I need to actually upload the document, I would consider adding a virus scanner here: */
    List<AttachmentUpload> uploads = ctx.attachments();
    if (uploads == null || uploads.isEmpty()) {
      return;
    }

    for (AttachmentUpload upload : uploads) {
      if (upload.file().getSize() > MAX_FILE_SIZE) {
        throw new ApiException(
            ErrorCode.FILE_TOO_LARGE,
            upload.file().getOriginalFilename() + " exceeds the 5MB limit");
      }
    }

    for (AttachmentUpload upload : uploads) {
      Attachment attachment = new Attachment();
      attachment.setFilename(upload.file().getOriginalFilename());
      attachment.setMimeType(upload.file().getContentType());
      attachment.setFileSize(upload.file().getSize());
      attachment.setFilePath(upload.file().getOriginalFilename());
      attachment.setUploadedBy(ctx.actor());
      attachmentRepository.save(attachment);

      ApplicationAttachment applicationAttachment = new ApplicationAttachment();
      applicationAttachment.setApplication(ctx.application());
      applicationAttachment.setAttachment(attachment);
      applicationAttachment.setDocumentType(upload.documentType());
      applicationAttachmentRepository.save(applicationAttachment);
    }

    log.info(
        "Saved {} attachment(s) for application {}",
        uploads.size(),
        ctx.application().getApplicationNumber());
  }
}
