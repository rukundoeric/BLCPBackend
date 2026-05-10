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
import rw.blcp.backend.core.application.enums.EApplicationStatus;
import rw.blcp.backend.core.auth.entity.User;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
public class Application extends BaseEntity {

  @Column(name = "application_number", nullable = false, unique = true)
  private String applicationNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "applicant_id")
  private User applicant;

  @Column(name = "applicant_email", nullable = false)
  private String applicantEmail;

  @Column(name = "applicant_first_name", nullable = false)
  private String applicantFirstName;

  @Column(name = "applicant_last_name", nullable = false)
  private String applicantLastName;

  @Column(name = "bank_name", nullable = false)
  private String bankName;

  @Column(name = "bank_type", nullable = false)
  private String bankType;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EApplicationStatus status = EApplicationStatus.NEW;

  @Enumerated(EnumType.STRING)
  @Column(name = "processing_level", nullable = false)
  private EOfficerLevel processingLevel = EOfficerLevel.LEVEL_1;
}
