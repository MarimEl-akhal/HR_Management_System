package com.example.hrm_system.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApiError {
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "Employee not found with id: "),
    INVALID_GROSS_SALARY(HttpStatus.BAD_REQUEST, "grossSalary: must be greater than 0"),
    INVALID_DATES(HttpStatus.BAD_REQUEST, "birthDate: must be a past date"),
    INVALID_MANAGER(HttpStatus.BAD_REQUEST, "Employee cannot be their own manager"),
    MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND, "Manager not found with id: "),
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Department not found with id: "),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "Team not found with id: "),
    EXPERTISE_NOT_FOUND(HttpStatus.NOT_FOUND, "Expertise not found with name: "),
    INVALID_EMPLOYEE_DELETION(HttpStatus.CONFLICT, "Can't remove  manager (has no manager)"), // Can't remove manager ->  has no  manager(root) ,has subordinates
    MISSING_NAME_FIELD_IN_JSON_BODY(HttpStatus.BAD_REQUEST, "name: Name is required"),
    INVALID_NAME(HttpStatus.BAD_REQUEST, "Name must not be null or empty");
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ApiError(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

}