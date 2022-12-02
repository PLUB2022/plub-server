package plub.plubserver.domain.account.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import plub.plubserver.common.dto.ApiResponse;

@Slf4j
@RestControllerAdvice
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AuthExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ApiResponse<?> handle(AuthException ex){
        log.warn("예외 발생 및 처리 = {} : {}",ex.getClass().getName(), ex.getMessage());
        return ApiResponse.error(ex.authError.getStatusCode(), ex.getMessage());
    }
}
