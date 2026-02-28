package com.example.hrm_system.entity;

import com.example.hrm_system.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;


    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;


    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "gross_salary")
    private BigDecimal grossSalary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @OneToMany(mappedBy = "manager")
    private Set<Employee> subordinates = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToMany
    @JoinTable(
            name = "employee_expertise", // The Join Table Name
            joinColumns = @JoinColumn(name = "employee_id"), // FK to Employee
            inverseJoinColumns = @JoinColumn(name = "expertise_id") // FK to Expertise
    )
    private Set<Expertise> expertises = new HashSet<>();


}
