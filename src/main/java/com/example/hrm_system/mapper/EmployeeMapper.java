package com.example.hrm_system.mapper;



import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.entity.Expertise;

import java.util.Set;
import java.util.stream.Collectors;


public class EmployeeMapper {
    public static Employee toEntity(EmployeeRequest request) {

        return Employee.builder()
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .graduationDate(request.getGraduationDate())
                .gender(request.getGender())
                .grossSalary(request.getGrossSalary())
                .build();
    }

    public static EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .birthDate(employee.getBirthDate())
                .graduationDate(employee.getGraduationDate())
                .gender(employee.getGender())
                .grossSalary(employee.getGrossSalary())
                .managerId(employee.getManager() != null
                        ? employee.getManager().getId()
                        : null)
                .departmentId(employee.getDepartment() != null
                        ? employee.getDepartment().getId()
                        : null)
                .teamId(employee.getTeam() != null
                        ? employee.getTeam().getId()
                        : null)
                .expertises(employee.getExpertises() != null
                        ? employee.getExpertises()
                        .stream()
                        .map(Expertise::getName)
                        .collect(Collectors.toSet())
                        : Set.of())

                .build();
    }
}
