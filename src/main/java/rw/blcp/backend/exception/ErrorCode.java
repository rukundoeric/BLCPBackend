package rw.blcp.backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  INVALID_CREDENTIALS("Username or password is incorrect", HttpStatus.UNAUTHORIZED),
  TOKEN_EXPIRED("Authentication token has expired", HttpStatus.UNAUTHORIZED),
  TOKEN_INVALID("Authentication token is invalid", HttpStatus.UNAUTHORIZED),
  ACCESS_DENIED("You do not have permission to perform this action", HttpStatus.FORBIDDEN),

  VALIDATION_FAILED("One or more fields failed validation", HttpStatus.BAD_REQUEST),

  RESOURCE_NOT_FOUND("The requested resource was not found", HttpStatus.NOT_FOUND),
  APPLICATION_NOT_FOUND("Application not found", HttpStatus.NOT_FOUND),
  RESOURCE_ALREADY_EXISTS(
      "A resource with the same identifier already exists", HttpStatus.CONFLICT),

  INVALID_STATE_TRANSITION(
      "This action is not allowed in the current application state",
      HttpStatus.UNPROCESSABLE_ENTITY),
  CONCURRENT_MODIFICATION(
      "The application was modified by another user, please retry", HttpStatus.CONFLICT),
  REVIEWER_APPROVER_CONFLICT(
      "The reviewer of an application cannot be its final approver", HttpStatus.FORBIDDEN),
  FINAL_STATE_IMMUTABLE("A final decision cannot be changed", HttpStatus.UNPROCESSABLE_ENTITY),

  FILE_TOO_LARGE("File exceeds the maximum allowed size of 5MB", HttpStatus.PAYLOAD_TOO_LARGE),
  INVALID_FILE_TYPE("The uploaded file type is not supported", HttpStatus.BAD_REQUEST),

  INTERNAL_ERROR(
      "An unexpected error occurred, please try again later", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String message;
  private final HttpStatus httpStatus;
}
