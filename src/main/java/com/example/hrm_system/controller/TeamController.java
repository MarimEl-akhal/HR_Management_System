package com.example.hrm_system.controller;

import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.service.TeamService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/{teamId}/employees")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployeesByTeamId(
            @PathVariable Long teamId,
            @RequestParam(required = false, defaultValue = "0") int pageNo,
            @RequestParam(required = false, defaultValue = "5") int pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {

        Page<EmployeeResponse> responses =
                teamService.getAllEmployeesByTeamId(teamId, PageRequest.of(pageNo, pageSize, Sort.by(direction, sortField)));

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

}