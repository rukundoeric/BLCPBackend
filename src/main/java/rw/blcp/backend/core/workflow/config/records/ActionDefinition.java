package rw.blcp.backend.core.workflow.config.records;

import rw.blcp.backend.core.workflow.enums.EActionType;

public record ActionDefinition(EActionType actionType, Object args) {

  public static ActionDefinition of(EActionType actionType) {
    return new ActionDefinition(actionType, null);
  }

  public static ActionDefinition of(EActionType actionType, Object args) {
    return new ActionDefinition(actionType, args);
  }
}
