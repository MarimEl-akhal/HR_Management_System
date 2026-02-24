package com.example.hrm_system.dto;


import com.example.hrm_system.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeResponse {
    private Long id;
    private String name;
    private LocalDate birthDate;
    private LocalDate graduationDate;
    private Gender gender;
    private BigDecimal grossSalary;
    private Long managerId;
    private Long departmentId;
    private Long teamId;
    private Set<String> expertises;
}
