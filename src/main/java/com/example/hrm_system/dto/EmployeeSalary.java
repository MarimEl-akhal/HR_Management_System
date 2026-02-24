package com.example.hrm_system.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeSalary {
    @Positive
    private BigDecimal grossSalary;
    @Positive
    private BigDecimal netSalary;
}
