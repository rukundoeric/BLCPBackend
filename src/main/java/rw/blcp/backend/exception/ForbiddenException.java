package rw.blcp.backend.exception;

public class ForbiddenException extends ApiException {
  public ForbiddenException() {
    super(ErrorCode.ACCESS_DENIED);
  }

  public ForbiddenException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ForbiddenException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
