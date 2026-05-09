package rw.blcp.backend.workflow.engine;

import rw.blcp.backend.workflow.engine.records.TransitionContext;
import rw.blcp.backend.workflow.enums.EActionType;

public interface Action<A> {

    EActionType getType();

    void execute(TransitionContext ctx, A args);
}
