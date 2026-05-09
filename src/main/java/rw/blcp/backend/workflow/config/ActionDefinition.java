package rw.blcp.backend.workflow.config;

public record ActionDefinition(EActionType actionType, Object args) {

    public static ActionDefinition of(EActionType actionType) {
        return new ActionDefinition(actionType, null);
    }

    public static ActionDefinition of(EActionType actionType, Object args) {
        return new ActionDefinition(actionType, args);
    }
}
