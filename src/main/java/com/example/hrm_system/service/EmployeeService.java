package com.example.hrm_system.service;

import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.exception.ApiException;
import com.example.hrm_system.mapper.EmployeeMapper;
import com.example.hrm_system.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static com.example.hrm_system.enums.ApiError.EMPLOYEE_NOT_FOUND;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public EmployeeResponse addEmployee(EmployeeRequest employeeRequestDto) {
        Employee employee = EmployeeMapper.toEntity(employeeRequestDto);
//        Employee savedEmployee = employee; // employeeRepository.save(employee);
//        employee.setId(12L);
//        return EmployeeMapper.toResponse(savedEmployee);
        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }


    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND, "Employee not found with id: " + id));
        return EmployeeMapper.toResponse(employee);
    }
}
