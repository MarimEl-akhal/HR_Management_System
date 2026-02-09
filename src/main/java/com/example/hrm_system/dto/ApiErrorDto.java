package com.example.hrm_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ApiErrorDto {
    private HttpStatus httpStatus;
    private String errorMessage;
}


