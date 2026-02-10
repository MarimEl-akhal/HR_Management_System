package com.example.hrm_system.service;

import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
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
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static com.example.hrm_system.enums.ApiError.EMPLOYEE_NOT_FOUND;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final ExpertiseRepository expertiseRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           TeamRepository teamRepository,
                           ExpertiseRepository expertiseRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.teamRepository = teamRepository;
        this.expertiseRepository = expertiseRepository;
    }

    public EmployeeResponse addEmployee(EmployeeRequest employeeRequestDto) {
        Employee employee = EmployeeMapper.toEntity(employeeRequestDto);

        if (employeeRequestDto.getManagerId() != null) {
            Employee manager = employeeRepository.findById(employeeRequestDto.getManagerId())
                    .orElseThrow(() -> new ApiException(
                            ApiError.MANAGER_NOT_FOUND,
                            "Manager not found with id: " + employeeRequestDto.getManagerId()
                    ));
            employee.setManager(manager);
        }


        if (employeeRequestDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeRequestDto.getDepartmentId())
                    .orElseThrow(() -> new ApiException(
                            ApiError.DEPARTMENT_NOT_FOUND,
                            "Department not found with id: " + employeeRequestDto.getDepartmentId()
                    ));
            employee.setDepartment(department);
        }


        if (employeeRequestDto.getTeamId() != null) {
            Team team = teamRepository.findById(employeeRequestDto.getTeamId())
                    .orElseThrow(() -> new ApiException(
                            ApiError.TEAM_NOT_FOUND,
                            "Team not found with id: " + employeeRequestDto.getTeamId()
                    ));
            employee.setTeam(team);
        }


        if (employeeRequestDto.getExpertises() != null && !employeeRequestDto.getExpertises().isEmpty()) {
            Set<Expertise> expertises = employeeRequestDto.getExpertises()
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
}
