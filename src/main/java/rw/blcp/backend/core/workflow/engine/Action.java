package rw.blcp.backend.core.workflow.engine;

import rw.blcp.backend.core.workflow.engine.records.TransitionContext;
import rw.blcp.backend.core.workflow.enums.EActionType;

public interface Action<A> {

  EActionType getType();

  void execute(TransitionContext ctx, A args);
}
