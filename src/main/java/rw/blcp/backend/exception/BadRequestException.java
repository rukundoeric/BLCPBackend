package rw.blcp.backend.exception;

public class BadRequestException extends ApiException {
  public BadRequestException() {
    super(ErrorCode.VALIDATION_FAILED);
  }

  public BadRequestException(String message) {
    super(ErrorCode.VALIDATION_FAILED, message);
  }
}
