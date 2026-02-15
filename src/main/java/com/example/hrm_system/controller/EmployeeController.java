package com.example.hrm_system.controller;

import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> addEmployee(@Valid @RequestBody EmployeeRequest employeeRequestDto) {
        EmployeeResponse response = employeeService.addEmployee(employeeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findEmployeeById(@PathVariable Long id) {
        EmployeeResponse responseDto = employeeService.findById(id);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest employeeRequest) {
        EmployeeResponse response = employeeService.updateEmployee(id, employeeRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}