package rw.blcp.backend.exception;

public class ConflictException extends ApiException {
  public ConflictException() {
    super(ErrorCode.RESOURCE_ALREADY_EXISTS);
  }

  public ConflictException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ConflictException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
