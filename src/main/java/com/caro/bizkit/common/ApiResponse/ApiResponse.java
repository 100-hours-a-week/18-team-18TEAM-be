package com.caro.bizkit.common.ApiResponse;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class ApiResponse<T> {
    private HttpStatusCode code;
    private String message;
    private T data;


    @Builder
    public ApiResponse(HttpStatusCode code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }



    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .build();
    }




    public static <T> ApiResponse<T> failed(HttpStatusCode code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

}

