package rw.blcp.backend.core.workflow.config.records;

import rw.blcp.backend.core.application.enums.EApplicationEvent;
import rw.blcp.backend.core.application.enums.EApplicationStatus;

public record TransitionKey(EApplicationEvent event, EApplicationStatus fromState) {}
