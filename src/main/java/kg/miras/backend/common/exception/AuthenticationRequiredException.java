package kg.miras.backend.common.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationRequiredException extends ApiException {
    public AuthenticationRequiredException(String message) {
        super(HttpStatus.UNAUTHORIZED, "authentication_required", message);
    }
}
