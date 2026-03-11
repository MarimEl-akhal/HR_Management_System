package com.example.hrm_system.service;

import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.dto.PagingResult;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.entity.Team;
import com.example.hrm_system.exception.ApiException;
import com.example.hrm_system.mapper.EmployeeMapper;
import com.example.hrm_system.repository.EmployeeRepository;
import com.example.hrm_system.repository.TeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.hrm_system.enums.ApiError.TEAM_NOT_FOUND;

@Service
@AllArgsConstructor
public class TeamService {

    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;


    public PagingResult<EmployeeResponse> getAllEmployeesByTeamId(Long teamId, Pageable pageable) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(TEAM_NOT_FOUND,
                        TEAM_NOT_FOUND.getDefaultMessage() + teamId));
        Page<Employee> employeePage = employeeRepository.findAllEmployeesByTeamId(team.getId(), pageable);
        List<EmployeeResponse> employeeResponses = employeePage.getContent().stream().map(EmployeeMapper::toResponse).toList();
        return new PagingResult<>(
                employeeResponses,
                employeePage.getTotalPages(),
                employeePage.getTotalElements(),
                employeePage.getSize(),
                employeePage.getNumber(),
                employeePage.isEmpty()
        );
    }
}


