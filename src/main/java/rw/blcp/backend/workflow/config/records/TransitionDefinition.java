package rw.blcp.backend.workflow.config.records;

import java.util.List;
import rw.blcp.backend.workflow.enums.EApplicationEvent;
import rw.blcp.backend.workflow.enums.EApplicationStatus;
import rw.blcp.backend.workflow.enums.EOfficerLevel;

public record TransitionDefinition(
        EApplicationEvent event,
        EApplicationStatus fromState,
        EApplicationStatus toState,
        EOfficerLevel processingLevel,
        List<ActionDefinition> breakingActions,
        List<ActionDefinition> nonBreakingActions) {}
