package plub.plubserver.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import plub.plubserver.common.dto.ApiResponse;
import plub.plubserver.domain.account.exception.AccountException;
import plub.plubserver.domain.account.exception.AuthException;
import plub.plubserver.domain.archive.exception.ArchiveException;
import plub.plubserver.domain.calendar.exception.CalendarException;
import plub.plubserver.domain.category.exception.CategoryException;
import plub.plubserver.domain.feed.exception.FeedException;
import plub.plubserver.domain.notice.exception.NoticeException;
import plub.plubserver.domain.notification.exception.NotificationException;
import plub.plubserver.domain.plubbing.exception.PlubbingException;
import plub.plubserver.domain.recruit.exception.RecruitException;
import plub.plubserver.domain.todo.exception.TodoException;
import plub.plubserver.util.s3.exception.AwsS3Exception;

import javax.validation.ConstraintViolationException;
import java.io.IOException;

import static plub.plubserver.common.dto.ApiResponse.error;
import static plub.plubserver.common.exception.CommonErrorCode.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResponse<?> globalHandle(Exception ex) {
        log.warn("글로벌 {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        return error(COMMON_BAD_REQUEST.getStatusCode(), ex.getMessage());
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResponse<?> errorHandle(IOException ex) {
        log.warn("{} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        return error(INVALID_INPUT_VALUE.getStatusCode(), ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    protected ApiResponse<?> handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException ex) {
        log.warn("{} - {}", ex.getMessage(), ex.getMessage());
        return error(METHOD_NOT_ALLOWED.getStatusCode(), ex.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResponse<?> httpClientErrorException(HttpClientErrorException ex) {
        log.warn("{} - {}", ex.getClass().getName(), ex.getMessage());
        return error(HTTP_CLIENT_ERROR.getStatusCode(), ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected ApiResponse<?> validationException(final ConstraintViolationException ex) {
        log.warn("{} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        return error(INVALID_INPUT_VALUE.getStatusCode(), ex.getConstraintViolations().iterator().next().getMessage());
    }

    // @Valid 실패시 이 예외가 터져서 잡아줘야 함
    @ExceptionHandler(BindException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected ApiResponse<?> validationException(final BindException ex) {
        log.warn("ValidationException({}) - {}", ex.getClass().getSimpleName(), ex.getMessage());
        StringBuilder reason = new StringBuilder();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            reason.append(fieldError.getDefaultMessage()).append(",");
        }
        return error(INVALID_INPUT_VALUE.getStatusCode(), reason.toString());
    }

    // Aws S3 Error
    @ExceptionHandler(AwsS3Exception.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected ApiResponse<?> awsS3Error(final AwsS3Exception ex) {
        log.warn("{} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        return error(AWS_S3_UPLOAD_FAIL.getStatusCode(), ex.getMessage());
    }

    // 파일 업로드 용량 초과시 발생
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected ApiResponse<?> handleMaxUploadSizeException(final MaxUploadSizeExceededException ex) {
        log.warn("{} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        return error(AWS_S3_FILE_SIZE_EXCEEDED.getStatusCode(), AWS_S3_FILE_SIZE_EXCEEDED.getMessage());
    }

    // LocalDateTime 파싱 에러
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected ApiResponse<?> handleHttpMessageNotReadableException(final HttpMessageNotReadableException ex) {
        log.warn("{} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        return error(INVALID_INPUT_VALUE.getStatusCode(), INVALID_INPUT_VALUE.getMessage());
    }

    /**
     * 도메인 예외 처리
     */
    @ExceptionHandler(AccountException.class)
    public ResponseEntity<?> handle(AccountException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.accountError.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.accountError.getHttpCode())
                .body(ApiResponse.error(ex.accountError.getStatusCode(), ex.accountError.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<?> handle(AuthException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.authError.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.authError.getHttpCode())
                .body(ApiResponse.error(ex.authError.getStatusCode(), ex.data, ex.authError.getMessage() + ex.message));
    }

    @ExceptionHandler(ArchiveException.class)
    public ResponseEntity<?> handle(ArchiveException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.archiveError.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.archiveError.getHttpCode())
                .body(ApiResponse.error(ex.archiveError.getStatusCode(), ex.archiveError.getMessage() + ex.getMessage()));
    }

    @ExceptionHandler(CalendarException.class)
    public ResponseEntity<?> handle(CalendarException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.calendarCode.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.calendarCode.getHttpCode())
                .body(ApiResponse.error(ex.calendarCode.getStatusCode(), ex.calendarCode.getMessage()));
    }

    @ExceptionHandler(CategoryException.class)
    public ResponseEntity<?> handle(CategoryException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.categoryError.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.categoryError.getHttpCode())
                .body(ApiResponse.error(ex.categoryError.getStatusCode(), ex.categoryError.getMessage()));
    }

    @ExceptionHandler(FeedException.class)
    public ResponseEntity<?> handle(FeedException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.feedCode.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.feedCode.getHttpCode())
                .body(ApiResponse.error(ex.feedCode.getStatusCode(), ex.feedCode.getMessage()));
    }

    @ExceptionHandler(NoticeException.class)
    public ResponseEntity<?> handle(NoticeException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.noticeCode.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.noticeCode.getHttpCode())
                .body(ApiResponse.error(ex.noticeCode.getStatusCode(), ex.noticeCode.getMessage()));
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<?> handle(NotificationException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.notificationCode.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.notificationCode.getHttpCode())
                .body(ApiResponse.error(ex.notificationCode.getStatusCode(), ex.notificationCode.getMessage()));
    }

    @ExceptionHandler(PlubbingException.class)
    public ResponseEntity<?> handle(PlubbingException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.plubbingCode.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.plubbingCode.getHttpCode())
                .body(ApiResponse.error(ex.plubbingCode.getStatusCode(), ex.plubbingCode.getMessage()));
    }

    @ExceptionHandler(RecruitException.class)
    public ResponseEntity<?> handle(RecruitException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.recruitCode.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.recruitCode.getHttpCode())
                .body(ApiResponse.error(ex.recruitCode.getStatusCode(), ex.recruitCode.getMessage()));
    }

    @ExceptionHandler(TodoException.class)
    public ResponseEntity<?> handle(TodoException ex) {
        log.warn("{}({}) - {}", ex.getClass().getSimpleName(), ex.todoCode.getHttpCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.todoCode.getHttpCode())
                .body(ApiResponse.error(ex.todoCode.getStatusCode(), ex.todoCode.getMessage()));
    }
}
