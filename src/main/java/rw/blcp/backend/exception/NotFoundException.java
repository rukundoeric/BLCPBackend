package rw.blcp.backend.exception;

public class NotFoundException extends ApiException {
  public NotFoundException() {
    super(ErrorCode.RESOURCE_NOT_FOUND);
  }

  public NotFoundException(String message) {
    super(ErrorCode.RESOURCE_NOT_FOUND, message);
  }
}
