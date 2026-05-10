package rw.blcp.backend.core.application.record;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rw.blcp.backend.core.application.enums.EApplicationEvent;

public record TakeActionRequest(
    @NotNull(message = "Event is required") EApplicationEvent event,
    @Size(max = 2000, message = "Comment must not exceed 2000 characters") String comment) {}
