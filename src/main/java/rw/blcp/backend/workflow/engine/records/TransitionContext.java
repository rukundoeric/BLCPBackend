package rw.blcp.backend.workflow.engine.records;

import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.workflow.entity.Application;

public record TransitionContext(
        Application application,
        User actor,
        String comment) {}
