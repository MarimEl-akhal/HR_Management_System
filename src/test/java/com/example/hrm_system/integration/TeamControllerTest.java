package com.example.hrm_system.integration;

import com.example.hrm_system.configuration.JacksonConfiguration;
import com.example.hrm_system.dto.EmployeeResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static com.example.hrm_system.enums.ApiError.TEAM_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@DbUnitConfiguration(databaseConnection = "dataSource")
@TestPropertySource(locations = "classpath:application-test.yml")
public class TeamControllerTest {
    private static final Long NO_EXIST_TEAM_ID = 99L;
    private static final Long EXIST_TEAM1_ID = 1L;
    private static final Long EXIST_EMPTY_TEAM_ID = 3L;


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JacksonConfiguration jacksonConfiguration;


    @Test
    @Transactional
    @DatabaseSetup("/dataset/get-employees-in-some-team.xml")
    public void testGetAllEmployeesInSomeTeam_whenTeamExists_shouldSuccessAndReturnEmployeesInTeam() throws Exception {
        final Set<String> EXPECTED_EMPLOYEES_NAMES = Set.of("Zaid", "Salim", "Laila");

        MvcResult result = mockMvc.perform(get("/api/teams/" + EXIST_TEAM1_ID + "/employees"))
                .andExpect(status().isOk())
                .andReturn();
        Set<EmployeeResponse> employeeResponses = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        Set<String> actualEmployeesNames = employeeResponses.stream()
                .map(EmployeeResponse::getName)
                .collect(Collectors.toSet());

        assertNotNull(employeeResponses);
        assertEquals(EXPECTED_EMPLOYEES_NAMES, actualEmployeesNames);
        assertEquals(EXPECTED_EMPLOYEES_NAMES.size(), actualEmployeesNames.size());

    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/get-employees-in-some-team.xml")
    public void testGetAllEmployeesInSomeTeam_whenTeamHasNoEmployees_shouldReturnEmptySet() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/teams/" + EXIST_EMPTY_TEAM_ID + "/employees"))
                .andExpect(status().isOk())
                .andReturn();
        Set<EmployeeResponse> employeeResponses = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertNotNull(employeeResponses);
        assertTrue(employeeResponses.isEmpty());
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/get-employees-in-some-team.xml")
    public void testGetAllEmployeesInSomeTeam_whenNotFoundTeam_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/teams/" + NO_EXIST_TEAM_ID + "/employees"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(TEAM_NOT_FOUND.getDefaultMessage() + NO_EXIST_TEAM_ID)));

    }

}
