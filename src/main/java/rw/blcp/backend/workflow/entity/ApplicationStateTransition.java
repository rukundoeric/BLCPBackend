package rw.blcp.backend.workflow.entity;

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
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.core.entity.BaseEntity;
import rw.blcp.backend.workflow.enums.EApplicationEvent;
import rw.blcp.backend.workflow.enums.EApplicationStatus;

@Entity
@Table(name = "application_state_transitions")
@Getter
@Setter
@NoArgsConstructor
public class ApplicationStateTransition extends BaseEntity {

  @Column(name = "application_number", nullable = false)
  private String applicationNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", nullable = false)
  private Application application;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EApplicationEvent event;

  @Enumerated(EnumType.STRING)
  @Column(name = "initial_state", nullable = false)
  private EApplicationStatus initialState;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_state", nullable = false)
  private EApplicationStatus newState;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id")
  private User actor;
}
