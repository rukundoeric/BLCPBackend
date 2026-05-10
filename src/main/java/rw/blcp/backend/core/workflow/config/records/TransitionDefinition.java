package rw.blcp.backend.core.workflow.config.records;

import java.util.List;
import rw.blcp.backend.core.application.enums.EApplicationEvent;
import rw.blcp.backend.core.application.enums.EApplicationStatus;
import rw.blcp.backend.core.officer.enums.EOfficerLevel;

public record TransitionDefinition(
    EApplicationEvent event,
    EApplicationStatus fromState,
    EApplicationStatus toState,
    EOfficerLevel processingLevel,
    List<ActionDefinition> breakingActions,
    List<ActionDefinition> nonBreakingActions) {}
