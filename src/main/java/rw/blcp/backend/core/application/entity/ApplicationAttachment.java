package rw.blcp.backend.core.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.blcp.backend.common.entity.BaseEntity;
import rw.blcp.backend.core.application.enums.EDocumentType;

@Entity
@Table(name = "application_attachments")
@Getter
@Setter
@NoArgsConstructor
public class ApplicationAttachment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", nullable = false)
  private Application application;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attachment_id", nullable = false)
  private Attachment attachment;

  @Enumerated(EnumType.STRING)
  @Column(name = "document_type", nullable = false)
  private EDocumentType documentType;
}
