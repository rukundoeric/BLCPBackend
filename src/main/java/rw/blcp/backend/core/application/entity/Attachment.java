package rw.blcp.backend.core.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.blcp.backend.common.entity.BaseEntity;
import rw.blcp.backend.core.auth.entity.User;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
public class Attachment extends BaseEntity {

  @Column(nullable = false)
  private String filename;

  @Column(name = "mime_type", nullable = false)
  private String mimeType;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "file_path", nullable = false)
  private String filePath;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by_id")
  private User uploadedBy;
}
