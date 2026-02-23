package com.example.hrm_system.dto;

import com.example.hrm_system.enums.Gender;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateEmployeeRequest {
    @Size(min = 3, message = "Name must be at least 3 characters long")
    private String name;

    @Past
    private LocalDate birthDate;

    private LocalDate graduationDate;

    private Gender gender;

    @Positive
    private BigDecimal grossSalary;

    private Long managerId;

    private Long departmentId;

    private Long teamId;

    private Set<String> expertises;
}