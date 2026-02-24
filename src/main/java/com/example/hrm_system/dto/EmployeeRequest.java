package com.example.hrm_system.dto;


import com.example.hrm_system.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Birth date is required")
    @Past
    private LocalDate birthDate;

    @NotNull(message = "Graduation date is required")
    private LocalDate graduationDate;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Gross salary is required")
    @Positive
    private BigDecimal grossSalary;

    private Long managerId;

    private Long departmentId;

    private Long teamId;

    private Set<String> expertises;

}
