package rw.blcp.backend.workflow.config;

import rw.blcp.backend.workflow.enums.EApplicationEvent;
import rw.blcp.backend.workflow.enums.EApplicationStatus;

public record TransitionKey(EApplicationEvent event, EApplicationStatus fromState) {}
