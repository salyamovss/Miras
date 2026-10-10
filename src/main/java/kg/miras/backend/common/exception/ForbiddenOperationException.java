package kg.miras.backend.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenOperationException extends ApiException {
    public ForbiddenOperationException(String message) {
        super(HttpStatus.FORBIDDEN, "forbidden", message);
    }
}
