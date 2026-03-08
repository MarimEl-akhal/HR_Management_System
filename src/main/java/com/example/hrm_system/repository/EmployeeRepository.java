package com.example.hrm_system.repository;

import com.example.hrm_system.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Set<Employee> findAllEmployeesByTeamId(Long teamId);
}

