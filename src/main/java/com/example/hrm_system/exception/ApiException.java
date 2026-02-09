package com.example.hrm_system.exception;

import com.example.hrm_system.enums.ApiError;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ApiError apiError;

    public ApiException(ApiError apiError, String message) {
        super(message);
        this.apiError = apiError;
    }

    public ApiException(ApiError apiError) {
        super(apiError.getDefaultMessage());
        this.apiError = apiError;
    }

}