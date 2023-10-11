package kluster.klusterweb.config.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorCode {

    private final int status;
    private final String message;
    private final int code;

}
