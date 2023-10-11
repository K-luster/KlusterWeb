package kluster.klusterweb.config.exception;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionController {
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> runtimeExHandler(ApiException e){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .status(e.getErrorCode().getStatus())
                .message(e.getErrorCode().getMessage()).build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse illegalExHandler(IllegalArgumentException e) {
        return new ErrorResponse(e.getMessage(), 500, 500);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public ErrorResponse illegalRuntimeExHandler(RuntimeException e) {
        return new ErrorResponse(e.getMessage(), 500, 500);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExpiredJwtException.class)
    public ErrorResponse illegalExpiredRuntimeExHandler(ExpiredJwtException e) {
        return new ErrorResponse(e.getMessage(), 400, 400);
    }
}
