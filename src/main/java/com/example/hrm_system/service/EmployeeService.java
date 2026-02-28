package com.example.hrm_system.service;

import com.example.hrm_system.configuration.JacksonConfiguration;
import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
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
    private final JacksonConfiguration jacksonConfiguration;


    public EmployeeResponse addEmployee(EmployeeRequest employeeRequest) {
        Employee employee = EmployeeMapper.toEntity(employeeRequest);

        if (employeeRequest.getManagerId() != null) {
            Employee manager = employeeRepository.findById(employeeRequest.getManagerId())
                    .orElseThrow(() -> new ApiException(
                            ApiError.MANAGER_NOT_FOUND,
                            MANAGER_NOT_FOUND.getDefaultMessage() + employeeRequest.getManagerId()
                    ));
            employee.setManager(manager);
        }


        if (employeeRequest.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeRequest.getDepartmentId())
                    .orElseThrow(() -> new ApiException(
                            ApiError.DEPARTMENT_NOT_FOUND,
                            DEPARTMENT_NOT_FOUND.getDefaultMessage() + employeeRequest.getDepartmentId()
                    ));
            employee.setDepartment(department);
        }


        if (employeeRequest.getTeamId() != null) {
            Team team = teamRepository.findById(employeeRequest.getTeamId())
                    .orElseThrow(() -> new ApiException(
                            ApiError.TEAM_NOT_FOUND,
                            TEAM_NOT_FOUND.getDefaultMessage() + employeeRequest.getTeamId()
                    ));
            employee.setTeam(team);
        }


        if (employeeRequest.getExpertises() != null && !employeeRequest.getExpertises().isEmpty()) {
            Set<Expertise> expertises = employeeRequest.getExpertises()
                    .stream()
                    .map(name -> expertiseRepository.findByName(name)
                            .orElseThrow(() -> new ApiException(
                                    ApiError.EXPERTISE_NOT_FOUND,
                                    EXPERTISE_NOT_FOUND.getDefaultMessage() + name
                            )))
                    .collect(Collectors.toSet());

            employee.setExpertises(expertises);
        }

        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }


    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                        EMPLOYEE_NOT_FOUND.getDefaultMessage() + id));
        return EmployeeMapper.toResponse(employee);
    }


    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                        EMPLOYEE_NOT_FOUND.getDefaultMessage() + id));

        Employee manager = employee.getManager();
        if (manager == null && !employee.getSubordinates().isEmpty()) {
            throw new ApiException(INVALID_EMPLOYEE_DELETION,
                    INVALID_EMPLOYEE_DELETION.getDefaultMessage());
        }
        for (Employee subordinate : employee.getSubordinates()) {
            subordinate.setManager(manager);
        }
        employeeRepository.delete(employee);
    }

    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest employeeRequest) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                        EMPLOYEE_NOT_FOUND.getDefaultMessage() + id));


        if (employeeRequest.getName() != null) {
            if (employeeRequest.getName().isPresent()) {
                employee.setName(employeeRequest.getName().get());
            } else {
                employee.setName(null);
            }
        }


        if (employeeRequest.getBirthDate() != null) {
            if (employeeRequest.getBirthDate().isPresent()) {
                employee.setBirthDate(employeeRequest.getBirthDate().get());
            } else {
                employee.setBirthDate(null);
            }
        }

        if (employeeRequest.getGraduationDate() != null) {
            if (employeeRequest.getGraduationDate().isPresent()) {
                employee.setGraduationDate(employeeRequest.getGraduationDate().get());
            } else {
                employee.setGraduationDate(null);
            }
        }

        if (employeeRequest.getGender() != null) {
            if (employeeRequest.getGender().isPresent()) {
                employee.setGender(employeeRequest.getGender().get());
            } else {
                employee.setGender(null);
            }
        }

        if (employeeRequest.getGrossSalary() != null) {
            if (employeeRequest.getGrossSalary().isPresent()) {
                employee.setGrossSalary(employeeRequest.getGrossSalary().get());
            } else {
                employee.setGrossSalary(null);
            }
        }
// Manager ID
        if (employeeRequest.getManagerId() != null) {
            if (employeeRequest.getManagerId().isPresent()) {
                Long managerId = employeeRequest.getManagerId().get();
                if (managerId == null) {
                    employee.setManager(null);  // Set to null when explicitly null
                } else {
                    if (managerId.equals(employee.getId())) throw new ApiException(INVALID_MANAGER);
                    Employee manager = employeeRepository.findById(managerId)
                            .orElseThrow(() -> new ApiException(MANAGER_NOT_FOUND,
                                    MANAGER_NOT_FOUND.getDefaultMessage() + managerId));
                    employee.setManager(manager);
                }
            } else {
                employee.setManager(null);  // JsonNullable.of(null) case
            }
        }


        if (employeeRequest.getDepartmentId() != null) {
            if (employeeRequest.getDepartmentId().isPresent()) {
                Long deptId = employeeRequest.getDepartmentId().get();
                if (deptId == null) {
                    employee.setDepartment(null);
                } else {
                    Department dept = departmentRepository.findById(deptId)
                            .orElseThrow(() -> new ApiException(DEPARTMENT_NOT_FOUND,
                                    DEPARTMENT_NOT_FOUND.getDefaultMessage() + deptId));
                    employee.setDepartment(dept);
                }
            } else {
                employee.setDepartment(null);
            }
        }


        if (employeeRequest.getTeamId() != null) {
            if (employeeRequest.getTeamId().isPresent()) {
                Long teamId = employeeRequest.getTeamId().get();
                if (teamId == null) {
                    employee.setTeam(null);
                } else {
                    Team team = teamRepository.findById(teamId)
                            .orElseThrow(() -> new ApiException(TEAM_NOT_FOUND,
                                    TEAM_NOT_FOUND.getDefaultMessage() + teamId));
                    employee.setTeam(team);
                }
            } else {
                employee.setTeam(null);
            }
        }

        if (employeeRequest.getExpertises() != null) {
            if (employeeRequest.getExpertises().isPresent()) {
                Set<String> expNames = employeeRequest.getExpertises().get();
                if (expNames == null || expNames.isEmpty()) {
                    employee.setExpertises(null);
                } else {
                    Set<Expertise> expertises = expNames.stream()
                            .map(name -> expertiseRepository.findByName(name)
                                    .orElseThrow(() -> new ApiException(EXPERTISE_NOT_FOUND,
                                            EXPERTISE_NOT_FOUND.getDefaultMessage() + name)))
                            .collect(Collectors.toSet());
                    employee.setExpertises(expertises);
                }
            } else {
                employee.setExpertises(null);
            }
        }

        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }
}


