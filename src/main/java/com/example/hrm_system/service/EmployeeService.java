package com.example.hrm_system.service;

import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.exception.ApiException;
import com.example.hrm_system.mapper.EmployeeMapper;
import com.example.hrm_system.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import static com.example.hrm_system.enums.ApiError.EMPLOYEE_NOT_FOUND;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND, "Employee not found with id: " + id));
        return EmployeeMapper.toResponse(employee);
    }
}
