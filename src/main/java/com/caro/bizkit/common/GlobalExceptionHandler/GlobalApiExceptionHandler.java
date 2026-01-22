package com.caro.bizkit.common.GlobalExceptionHandler;



import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.common.exception.CustomException;
import com.caro.bizkit.common.exception.KakaoOAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalApiExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException ex) {
        HttpStatusCode statusCode = ex.getStatus();

        String message = ex.getMessage();

        if (statusCode.is5xxServerError()) {
            log.error("Custom Error: {} | Status: {}", message, statusCode);
        } else {
            log.warn("Custom Error: {} | Status: {}", message, statusCode);
        }

        return ResponseEntity
                .status(statusCode)
                .body(ApiResponse.failed(statusCode, message));
    }
    @ExceptionHandler(KakaoOAuthException.class)
    public ResponseEntity<ApiResponse<String>> handleKakaoOAuthException(KakaoOAuthException ex) {
        HttpStatusCode statusCode = ex.getStatus();

        String message = ex.getMessage();
        String apiMessage = ex.getApiMessage();

        log.warn("Custom Error: {} | Status: {}", apiMessage, statusCode);


        return ResponseEntity
                .status(statusCode)
                .body(ApiResponse.failed(statusCode, message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<String>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatusCode statusCode = ex.getStatusCode();

        String message = (ex.getReason() != null) ? ex.getReason() : "요청 처리 중 오류가 발생했습니다.";

        if (statusCode.is5xxServerError()) {
            log.error("Server Error: {} | Status: {}", message, statusCode);
        } else {
            log.warn("Client Error: {} | Status: {}", message, statusCode);
        }


        return ResponseEntity
                .status(statusCode)
                .body(ApiResponse.failed(statusCode, message));
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;


        log.error("[Internal Server Error] Message: {} | Status: {}", ex.getMessage(), status.value());


        return ResponseEntity
                .status(status)
                .body(ApiResponse.failed(
                        HttpStatusCode.valueOf(status.value()),
                        "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."));
    }
}
