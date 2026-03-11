package com.example.hrm_system.service;

import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.entity.Team;
import com.example.hrm_system.exception.ApiException;
import com.example.hrm_system.mapper.EmployeeMapper;
import com.example.hrm_system.repository.EmployeeRepository;
import com.example.hrm_system.repository.TeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.hrm_system.enums.ApiError.TEAM_NOT_FOUND;

@Service
@AllArgsConstructor
public class TeamService {

    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;


    public List<EmployeeResponse> getAllEmployeesByTeamId(Long teamId, Pageable pageable) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(TEAM_NOT_FOUND,
                        TEAM_NOT_FOUND.getDefaultMessage() + teamId));
        List<Employee> employees = employeeRepository.findAllEmployeesByTeamId(team.getId(), pageable).getContent();
        return employees.stream().map(EmployeeMapper::toResponse).toList();
    }
}


