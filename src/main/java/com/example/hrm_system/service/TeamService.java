package com.example.hrm_system.service;

import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.entity.Team;
import com.example.hrm_system.exception.ApiException;
import com.example.hrm_system.mapper.EmployeeMapper;
import com.example.hrm_system.repository.EmployeeRepository;
import com.example.hrm_system.repository.TeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static com.example.hrm_system.enums.ApiError.TEAM_NOT_FOUND;

@Service
@AllArgsConstructor
public class TeamService {

    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;


    public Set<EmployeeResponse> getAllEmployeesByTeamId(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(TEAM_NOT_FOUND,
                        TEAM_NOT_FOUND.getDefaultMessage() + teamId));
        Set<Employee> employees = employeeRepository.findAllEmployeesByTeamId(team.getId());
        return employees.stream().map(EmployeeMapper::toResponse).collect(Collectors.toSet());
    }
}


