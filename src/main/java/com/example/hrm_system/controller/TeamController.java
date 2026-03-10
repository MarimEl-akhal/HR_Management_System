package com.example.hrm_system.controller;

import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.service.TeamService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/{teamId}/employees")
    public ResponseEntity<Set<EmployeeResponse>> getAllEmployeesByTeamId(@PathVariable Long teamId) {
        Set<EmployeeResponse> responses = teamService.getAllEmployeesByTeamId(teamId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }


}