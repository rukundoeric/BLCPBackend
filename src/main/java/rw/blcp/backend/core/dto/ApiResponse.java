package rw.blcp.backend.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String traceId;
    private final T data;
    private final Instant timestamp;

    private ApiResponse(T data) {
        this.traceId = MDC.get("traceId");
        this.data = data;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}
