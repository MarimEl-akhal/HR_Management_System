package com.example.hrm_system.dto;

import com.example.hrm_system.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEmployeeRequest {
    private String name;
    @Past
    private LocalDate birthDate;

    private LocalDate graduationDate;

    private Gender gender;

    @Positive
    private Double grossSalary;

    private Long managerId;

    private Long departmentId;

    private Long teamId;

    private Set<String> expertises;
}

