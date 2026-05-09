package rw.blcp.backend.workflow.engine;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import rw.blcp.backend.workflow.enums.EActionType;

@Component
public class ActionRegistry {

    private final Map<EActionType, Action<Object>> registry;

    @SuppressWarnings("unchecked")
    public ActionRegistry(List<Action<?>> actions) {
        this.registry = actions.stream()
                .collect(Collectors.toMap(Action::getType, action -> (Action<Object>) action));
    }

    public Action<Object> get(EActionType type) {
        Action<Object> action = registry.get(type);
        if (action == null) {
            throw new IllegalStateException("No action registered for type: " + type);
        }
        return action;
    }
}
