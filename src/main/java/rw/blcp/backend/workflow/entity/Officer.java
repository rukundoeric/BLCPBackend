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
import rw.blcp.backend.auth.entity.Role;
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.core.entity.BaseEntity;
import rw.blcp.backend.workflow.enums.EOfficerLevel;

@Entity
@Table(name = "officers")
@Getter
@Setter
@NoArgsConstructor
public class Officer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EOfficerLevel level;
}
