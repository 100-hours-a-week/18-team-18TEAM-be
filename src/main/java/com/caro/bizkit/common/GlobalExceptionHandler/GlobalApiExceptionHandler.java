package com.caro.bizkit.common.GlobalExceptionHandler;



import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.common.exception.CustomException;
import com.caro.bizkit.common.exception.KakaoOAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalApiExceptionHandler extends ResponseEntityExceptionHandler {



    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {




        String safeMessage = "요청이 올바르지 않습니다.";

        // 만약 여기서 500이 잡혔다면(JSON 변환 실패 등), 메시지를 숨겨야 함
        if (statusCode.is5xxServerError()) {
            safeMessage = "서버 내부 오류가 발생했습니다.";
        }
        log.error("Error: {} | Status: {} | at {}", ex.getMessage(), statusCode, ex.getStackTrace()[0]);

        return ResponseEntity
                .status(statusCode)
                .body(ApiResponse.failed(statusCode, safeMessage));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException ex) {
        HttpStatusCode statusCode = ex.getStatus();

        String message = ex.getMessage();

        if (statusCode.is5xxServerError()) {
            log.error("Custom Error: {} | Status: {} | at {}", message, statusCode, ex.getStackTrace()[0]);
        } else {
            log.warn("Custom Error: {} | Status: {} | at {}", message, statusCode, ex.getStackTrace()[0]);
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

        log.warn("Custom Error: {} | Status: {} | at {}", apiMessage, statusCode, ex.getStackTrace()[0]);


        return ResponseEntity
                .status(statusCode)
                .body(ApiResponse.failed(statusCode, message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<String>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatusCode statusCode = ex.getStatusCode();

        String message = (ex.getReason() != null) ? ex.getReason() : "요청 처리 중 오류가 발생했습니다.";

        if (statusCode.is5xxServerError()) {
            log.error("Server Error: {} | Status: {} | at {}", message, statusCode, ex.getStackTrace()[0]);
        } else {
            log.warn("Client Error: {} | Status: {} | at {}", message, statusCode, ex.getStackTrace()[0]);
        }


        return ResponseEntity
                .status(statusCode)
                .body(ApiResponse.failed(statusCode, message));
    }

//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<Object> handleBadJson(HttpMessageNotReadableException ex) {
//
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(ApiResponse.failed(HttpStatus.BAD_REQUEST, "json 형식이 맞지 않습니다."));
//    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;


        log.error("[Internal Server Error] Message: {} | Status: {} | at {}", ex.getMessage(), status.value(), ex.getStackTrace()[0]);


        return ResponseEntity
                .status(status)
                .body(ApiResponse.failed(
                        HttpStatusCode.valueOf(status.value()),
                        "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."));
    }
}
