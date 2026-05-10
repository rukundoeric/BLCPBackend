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
import rw.blcp.backend.core.application.enums.EPreferenceKey;

@Entity
@Table(name = "application_preferences")
@Getter
@Setter
@NoArgsConstructor
public class ApplicationPreference extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", nullable = false)
  private Application application;

  @Enumerated(EnumType.STRING)
  @Column(name = "preference_key", nullable = false)
  private EPreferenceKey preferenceKey;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String value;
}
