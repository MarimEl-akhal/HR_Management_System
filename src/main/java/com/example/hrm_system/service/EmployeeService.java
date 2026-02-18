package com.example.hrm_system.service;

import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.dto.UpdateEmployeeWorkDetail;
import com.example.hrm_system.dto.UpdateEmployeeRequest;
import com.example.hrm_system.entity.Department;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.entity.Expertise;
import com.example.hrm_system.entity.Team;
import com.example.hrm_system.enums.ApiError;
import com.example.hrm_system.exception.ApiException;
import com.example.hrm_system.mapper.EmployeeMapper;
import com.example.hrm_system.repository.DepartmentRepository;
import com.example.hrm_system.repository.EmployeeRepository;
import com.example.hrm_system.repository.ExpertiseRepository;
import com.example.hrm_system.repository.TeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static com.example.hrm_system.enums.ApiError.*;

@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final ExpertiseRepository expertiseRepository;


    public EmployeeResponse addEmployee(EmployeeRequest employeeRequest) {
        Employee employee = EmployeeMapper.toEntity(employeeRequest);

        if (employeeRequest.getManagerId() != null) {
            Employee manager = employeeRepository.findById(employeeRequest.getManagerId())
                    .orElseThrow(() -> new ApiException(
                            ApiError.MANAGER_NOT_FOUND,
                            "Manager not found with id: " + employeeRequest.getManagerId()
                    ));
            employee.setManager(manager);
        }


        if (employeeRequest.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeRequest.getDepartmentId())
                    .orElseThrow(() -> new ApiException(
                            ApiError.DEPARTMENT_NOT_FOUND,
                            "Department not found with id: " + employeeRequest.getDepartmentId()
                    ));
            employee.setDepartment(department);
        }


        if (employeeRequest.getTeamId() != null) {
            Team team = teamRepository.findById(employeeRequest.getTeamId())
                    .orElseThrow(() -> new ApiException(
                            ApiError.TEAM_NOT_FOUND,
                            "Team not found with id: " + employeeRequest.getTeamId()
                    ));
            employee.setTeam(team);
        }


        if (employeeRequest.getExpertises() != null && !employeeRequest.getExpertises().isEmpty()) {
            Set<Expertise> expertises = employeeRequest.getExpertises()
                    .stream()
                    .map(name -> expertiseRepository.findByName(name)
                            .orElseThrow(() -> new ApiException(
                                    ApiError.EXPERTISE_NOT_FOUND,
                                    "Expertise not found with name: " + name
                            )))
                    .collect(Collectors.toSet());

            employee.setExpertises(expertises);
        }

        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }


    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND, "Employee not found with id: " + id));
        return EmployeeMapper.toResponse(employee);
    }


    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        Employee manager = employee.getManager();
        if (manager == null && !employee.getSubordinates().isEmpty()) {
            throw new ApiException(INVALID_EMPLOYEE_DELETION);
        }
        for (Employee subordinate : employee.getSubordinates()) {
            subordinate.setManager(manager);
        }
        employeeRepository.delete(employee);
    }


    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest employeeRequest) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        Department department;
        if (employeeRequest.getDepartmentId() != null) {
            department = departmentRepository.findById(employeeRequest.getDepartmentId())
                    .orElseThrow(() -> new ApiException(DEPARTMENT_NOT_FOUND,
                            "Department not found with id: " + employeeRequest.getDepartmentId()));
        } else {
            department = null;
        }

        Team team;
        if (employeeRequest.getTeamId() != null) {
            team = teamRepository.findById(employeeRequest.getTeamId())
                    .orElseThrow(() -> new ApiException(TEAM_NOT_FOUND,
                            "Team not found with id: " + employeeRequest.getTeamId()));
        } else {
            team = null;
        }

        Employee manager;
        if (employeeRequest.getManagerId() != null) {
            if (employeeRequest.getManagerId().equals(id)) {
                throw new ApiException(INVALID_MANAGER);
            }
            manager = employeeRepository.findById(employeeRequest.getManagerId())
                    .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                            "Manager not found with id: " + employeeRequest.getManagerId()));
        } else {
            manager = null;
        }


        Set<Expertise> expertises;
        if (employeeRequest.getExpertises() != null && !employeeRequest.getExpertises().isEmpty()) {
            expertises = employeeRequest.getExpertises()
                    .stream()
                    .map(name -> expertiseRepository.findByName(name)
                            .orElseThrow(() -> new ApiException(
                                    ApiError.EXPERTISE_NOT_FOUND,
                                    "Expertise not found with name: " + name
                            )))
                    .collect(Collectors.toSet());
        } else {
            expertises = null;
        }

        UpdateEmployeeWorkDetail workDetail = UpdateEmployeeWorkDetail.builder()
                .manager(manager)
                .department(department)
                .team(team)
                .expertises(expertises)
                .build();
        Employee updateEmployee = updateEmployeeField(employee, employeeRequest, workDetail);
        Employee saveUpdatedEmployee = employeeRepository.save(updateEmployee);
        return EmployeeMapper.toResponse(saveUpdatedEmployee);
    }

    private Employee updateEmployeeField(Employee employee, UpdateEmployeeRequest request, UpdateEmployeeWorkDetail workDetail) {
        return Employee.builder()
                .id(employee.getId())
                .name(request.getName() != null ? request.getName() : employee.getName())
                .birthDate(request.getBirthDate() != null ? request.getBirthDate() : employee.getBirthDate())
                .graduationDate(request.getGraduationDate() != null ? request.getGraduationDate() : employee.getGraduationDate())
                .gender(request.getGender() != null ? request.getGender() : employee.getGender())
                .grossSalary(request.getGrossSalary() != null ? request.getGrossSalary() : employee.getGrossSalary())
                .manager(workDetail.getManager() != null ? workDetail.getManager() : employee.getManager())
                .department(workDetail.getDepartment() != null ? workDetail.getDepartment() : employee.getDepartment())
                .team(workDetail.getTeam() != null ? workDetail.getTeam() : employee.getTeam())
                .expertises(workDetail.getExpertises() != null ? workDetail.getExpertises() : employee.getExpertises())
                .build();

    }

}
