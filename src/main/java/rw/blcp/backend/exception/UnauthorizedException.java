package rw.blcp.backend.exception;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
