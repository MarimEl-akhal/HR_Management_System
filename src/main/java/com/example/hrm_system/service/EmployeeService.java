package com.example.hrm_system.service;

import com.example.hrm_system.configuration.JacksonConfiguration;
import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.dto.EmployeeSalary;
import com.example.hrm_system.entity.Department;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.entity.Expertise;
import com.example.hrm_system.entity.Team;
import com.example.hrm_system.enums.ApiError;
import com.example.hrm_system.enums.Gender;
import com.example.hrm_system.exception.ApiException;
import com.example.hrm_system.mapper.EmployeeMapper;
import com.example.hrm_system.repository.DepartmentRepository;
import com.example.hrm_system.repository.EmployeeRepository;
import com.example.hrm_system.repository.ExpertiseRepository;
import com.example.hrm_system.repository.TeamRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.hrm_system.enums.ApiError.*;

@Service
@AllArgsConstructor
public class EmployeeService {

    private static final BigDecimal TAX_RATIO = BigDecimal.valueOf(0.15);
    private static final BigDecimal TAX_REMAINDER = BigDecimal.ONE.subtract(TAX_RATIO);
    private static final BigDecimal INSURANCE_AMOUNT = BigDecimal.valueOf(500);


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


    public EmployeeResponse updateEmployee(Long id, Map<String, Object> updates) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND, "Employee not found with id: " + id));


        if (updates.containsKey("departmentId")) {
            if (updates.get("departmentId") == null) {
                employee.setDepartment(null);
            } else {
                Long deptId = jacksonConfiguration.objectMapper().convertValue(updates.get("departmentId"), Long.class);
                Department dept = departmentRepository.findById(deptId)
                        .orElseThrow(() -> new ApiException(DEPARTMENT_NOT_FOUND));
                employee.setDepartment(dept);
            }
        }

        if (updates.containsKey("teamId")) {
            if (updates.get("teamId") == null) {
                employee.setTeam(null);
            } else {
                Long teamId = jacksonConfiguration.objectMapper().convertValue(updates.get("teamId"), Long.class);
                Team team = teamRepository.findById(teamId)
                        .orElseThrow(() -> new ApiException(TEAM_NOT_FOUND));
                employee.setTeam(team);
            }
        }


        if (updates.containsKey("managerId")) {
            if (updates.get("managerId") == null) {
                employee.setManager(null);
            } else {
                Long managerId = jacksonConfiguration.objectMapper().convertValue(updates.get("managerId"), Long.class);
                if (managerId.equals(id)) throw new ApiException(INVALID_MANAGER);
                Employee manager = employeeRepository.findById(managerId)
                        .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND));
                employee.setManager(manager);
            }
        }

        if (updates.containsKey("name")) {
            String newName = (String) updates.get("name");
            if (newName != null) {
                employee.setName(newName);
            }
        }

        if (updates.containsKey("grossSalary")) {
            BigDecimal salary = jacksonConfiguration.objectMapper().convertValue(updates.get("grossSalary"), BigDecimal.class);
            employee.setGrossSalary(salary);
        }

        if (updates.containsKey("gender")) {
            Gender gender = jacksonConfiguration.objectMapper().convertValue(updates.get("gender"), Gender.class);
            employee.setGender(gender);
        }

        if (updates.containsKey("birthDate")) {
            LocalDate date = jacksonConfiguration.objectMapper().convertValue(updates.get("birthDate"), LocalDate.class);
            employee.setBirthDate(date);
        }
        if (updates.containsKey("graduationDate")) {
            LocalDate date = jacksonConfiguration.objectMapper().convertValue(updates.get("graduationDate"), LocalDate.class);
            employee.setGraduationDate(date);
        }

        if (updates.containsKey("expertises")) {
            if (updates.get("expertises") == null) {
                employee.setExpertises(null);
            } else {
                Set<String> names = jacksonConfiguration.objectMapper().convertValue(updates.get("expertises"), new TypeReference<>() {
                });
                Set<Expertise> expertises = names.stream()
                        .map(name -> expertiseRepository.findByName(name).orElseThrow())
                        .collect(Collectors.toSet());
                employee.setExpertises(expertises);
            }
        }

        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }

    public EmployeeSalary getEmployeeSalaryInfo(Long id){
        Employee employee = employeeRepository.findById(id).orElseThrow();

        /* net = grossSalary - (grossSalary*TAX_RATIO) - INSURANCE_AMOUNT
                = grossSalary(1-TAX_RATIO)-INSURANCE_AMOUNT
              = grossSalary(TAX_REMAINDER)-INSURANCE_AMOUNT */
        BigDecimal netSalary = employee.getGrossSalary().multiply(TAX_REMAINDER).subtract(INSURANCE_AMOUNT);
        return EmployeeSalary.builder()
                .grossSalary(employee.getGrossSalary())
                .netSalary(netSalary)
                .build();
    }


}
