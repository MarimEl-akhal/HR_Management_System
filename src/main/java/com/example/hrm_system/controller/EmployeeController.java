package com.example.hrm_system.controller;

import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.dto.EmployeeSalaryDto;
import com.example.hrm_system.dto.UpdateEmployeeRequest;
import com.example.hrm_system.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> addEmployee(@Valid @RequestBody EmployeeRequest employeeRequest) {
        EmployeeResponse response = employeeService.addEmployee(employeeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<EmployeeResponse> findEmployeeById(@PathVariable Long id) {
        EmployeeResponse response = employeeService.findById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/employees/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id, @RequestBody UpdateEmployeeRequest employeeRequest) {
        EmployeeResponse response = employeeService.updateEmployee(id, employeeRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/salary")
    public ResponseEntity<EmployeeSalaryDto> getEmployeeSalaryInfo(@PathVariable Long id) {
        EmployeeSalaryDto employeeSalaryResponse = employeeService.getEmployeeSalaryInfo(id);
        return ResponseEntity.status(HttpStatus.OK).body(employeeSalaryResponse);
    }

}