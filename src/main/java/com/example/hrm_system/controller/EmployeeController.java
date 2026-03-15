package com.example.hrm_system.controller;

import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.dto.EmployeeSalaryDto;
import com.example.hrm_system.dto.UpdateEmployeeRequest;
import com.example.hrm_system.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> addEmployee(@Valid @RequestBody EmployeeRequest employeeRequest) {
        EmployeeResponse response = employeeService.addEmployee(employeeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findEmployeeById(@PathVariable Long id) {
        EmployeeResponse response = employeeService.findById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id, @RequestBody UpdateEmployeeRequest employeeRequest) {
        EmployeeResponse response = employeeService.updateEmployee(id, employeeRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/salary")
    public ResponseEntity<EmployeeSalaryDto> getEmployeeSalaryInfo(@PathVariable Long id) {
        EmployeeSalaryDto employeeSalaryResponse = employeeService.getEmployeeSalaryInfo(id);
        return ResponseEntity.status(HttpStatus.OK).body(employeeSalaryResponse);
    }

    @GetMapping("/{managerId}/hierarchy")
    public ResponseEntity<Page<EmployeeResponse>> getEmployeesUnderSpecificManager(@PathVariable Long managerId,
                                                                                   @RequestParam(required = false, defaultValue = "0") int pageNo,
                                                                                   @RequestParam(required = false, defaultValue = "5") int pageSize,
                                                                                   @RequestParam(required = false, defaultValue = "id") String sortField,
                                                                                   @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction) {
        Page<EmployeeResponse> responses =
                employeeService.getAllEmployeesUnderSpecificManger(managerId, PageRequest.of(pageNo, pageSize, Sort.by(direction, sortField)));
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }


}