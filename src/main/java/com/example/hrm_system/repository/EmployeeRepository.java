package com.example.hrm_system.repository;

import com.example.hrm_system.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @EntityGraph(attributePaths = {"expertises","department","team"})
    Page<Employee> findByManagerId(Long managerID, Pageable pageable);
}

