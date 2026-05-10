package rw.blcp.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> details;
  }

  public static ApiErrorResponse of(String errorCode, String errorMessage) {
    return ApiErrorResponse.builder()
        .traceId(MDC.get("traceId"))
        .error(ErrorDetail.builder().errorCode(errorCode).errorMessage(errorMessage).build())
        .timestamp(Instant.now())
        .build();
  }

  public static ApiErrorResponse of(
      String errorCode, String errorMessage, Map<String, String> details) {
    return ApiErrorResponse.builder()
        .traceId(MDC.get("traceId"))
        .error(
            ErrorDetail.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .details(details)
                .build())
        .timestamp(Instant.now())
        .build();
  }
}
