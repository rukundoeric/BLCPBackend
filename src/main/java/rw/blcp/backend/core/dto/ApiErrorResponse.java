package rw.blcp.backend.web.dto;

import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;

import java.time.Instant;

@Getter
@Builder
public class ApiErrorResponse {

    private final String traceId;
    private final ErrorDetail error;
    private final Instant timestamp;

    @Getter
    @Builder
    public static class ErrorDetail {
        private final String errorCode;
        private final String errorMessage;
    }

    public static ApiErrorResponse of(String errorCode, String errorMessage) {
        return ApiErrorResponse.builder()
                .traceId(MDC.get("traceId"))
                .error(ErrorDetail.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build())
                .timestamp(Instant.now())
                .build();
    }
}
