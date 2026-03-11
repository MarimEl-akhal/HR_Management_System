package com.example.hrm_system.controller;

import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.service.TeamService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/{teamId}/employees")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployeesByTeamId(@PathVariable Long teamId,
                                                                          @RequestParam(required = false, defaultValue = "0") int pageNo,
                                                                          @RequestParam(required = false, defaultValue = "5") int pageSize
    ) {
        List<EmployeeResponse> responses = teamService.getAllEmployeesByTeamId(teamId, PageRequest.of(pageNo, pageSize));
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }


}