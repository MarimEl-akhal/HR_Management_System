package com.example.hrm_system.dto;

import com.example.hrm_system.entity.Department;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.entity.Expertise;
import com.example.hrm_system.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEmployeeWorkDetail {
    private Employee manager;
    private Department department;
    private Team team;
    private Set<Expertise> expertises;
}
