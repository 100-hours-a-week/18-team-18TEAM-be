package com.example.caro.common.ApiResponse;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class ApiResponse<T> {
    private String error;
    private String message;
    private T data;

    public ApiResponse(){
    }

    public ApiResponse(String error, String message, T data){
        this.error = error;
        this.message = message;
        this.data = data;
    }
    public ApiResponse(String message, T data ){
        this.message = message;
        this.data = data;
    }



    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }


    public static <T> ApiResponse<T> success(String message){
        return new ApiResponse<>(message,null);
    }

    public static <T> ApiResponse<T> failed(String error, T data){
        return new ApiResponse<>(error, null, data);
    }

}

