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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static com.example.hrm_system.enums.ApiError.EMPLOYEE_NOT_FOUND;
import static com.example.hrm_system.enums.ApiError.INVALID_EMPLOYEE_DELETION;

@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final ExpertiseRepository expertiseRepository;

    @Transactional
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

    @Transactional
    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND, "Employee not found with id: " + id));
        return EmployeeMapper.toResponse(employee);
    }

    @Transactional
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

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest employeeRequest) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow();
        updateEmployeeField(employee, employeeRequest);
        return EmployeeMapper.toResponse(employee);
    }

    private void updateEmployeeField(Employee employee, EmployeeRequest request) {
        if (request.getName() != null) {
            employee.setName(request.getName());
        }
        if (request.getBirthDate() != null) {
            employee.setBirthDate(request.getBirthDate());
        }
        if (request.getGraduationDate() != null) {
            employee.setGraduationDate(request.getGraduationDate());
        }
        if (request.getGender() != null) {
            employee.setGender(request.getGender());
        }
        if (request.getGrossSalary() != null) {
            employee.setGrossSalary(request.getGrossSalary());
        }
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow();
            employee.setManager(manager);
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow();
            employee.setDepartment(department);
        }
        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow();
            employee.setTeam(team);
        }
        if (request.getExpertises() != null) {
            Set<Expertise> expertises = request.getExpertises().stream()
                    .map(expertiseName -> expertiseRepository.findByName(expertiseName)
                            .orElseThrow())
                    .collect(Collectors.toSet());
            employee.setExpertises(expertises);
        }
    }
}
