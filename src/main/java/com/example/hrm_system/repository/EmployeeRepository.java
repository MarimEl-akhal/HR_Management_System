package com.example.hrm_system.repository;

import com.example.hrm_system.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query(nativeQuery = true,
            value = """
                    WITH RECURSIVE employee_hierarchy (employee_id, name, birth_date, graduation_date, gender, gross_salary,
                               manager_id, department_id, team_id) AS (
                        SELECT employee_id, name, birth_date, graduation_date, gender, gross_salary,
                               manager_id, department_id, team_id
                        FROM employees
                        WHERE manager_id = :managerId
                    
                        UNION ALL
                    
                        SELECT e.employee_id, e.name, e.birth_date, e.graduation_date, e.gender, e.gross_salary,
                               e.manager_id, e.department_id, e.team_id
                        FROM employees e
                        INNER JOIN employee_hierarchy eh
                            ON e.manager_id = eh.employee_id
                    )
                    SELECT *
                    FROM employee_hierarchy
                    """,
            countQuery = """
                    WITH RECURSIVE employee_hierarchy (
                        employee_id, name, birth_date, graduation_date, gender, gross_salary,
                        manager_id, department_id, team_id) AS (
                        SELECT employee_id, name, birth_date, graduation_date, gender, gross_salary,
                               manager_id, department_id, team_id
                        FROM employees
                        WHERE manager_id = :managerId
                    
                        UNION ALL
                    
                        SELECT e.employee_id, e.name, e.birth_date, e.graduation_date, e.gender, e.gross_salary,
                               e.manager_id, e.department_id, e.team_id
                        FROM employees e
                        INNER JOIN employee_hierarchy eh
                            ON e.manager_id = eh.employee_id
                    )
                    SELECT COUNT(*) FROM employee_hierarchy
                    """
    )
    Page<Employee> findByManagerId(@Param("managerId") Long managerID, Pageable pageable);
}
