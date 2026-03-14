package com.example.hrm_system.integration;

import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.repository.EmployeeRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
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
    private static final String FIRST_PAGE_NUMBER = "0";
    private static final String SECOND_PAGE_NUMBER = "1";
    private static final String PAGE_SIZE = "3";
    private static final String SORT_DIRECTION_DESC = "DESC";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;


    @Test
    @Transactional
    @DatabaseSetup("/dataset/get-employees-in-some-team.xml")
    public void testGetAllEmployeesInSomeTeamWithPagination_whenTeamExists_shouldSuccessAndReturnEmployeesInTeam() throws Exception {
        final int EXPECTED_CONTENT_SIZE_IN_FIRST_PAGE = 3;
        final int EXPECTED_CONTENT_SIZE_IN_SECOND_PAGE = 2;


        //first page
        mockMvc.perform(get("/api/teams/" + EXIST_TEAM1_ID + "/employees")
                        .param("pageNumber", FIRST_PAGE_NUMBER)
                        .param("pageSize", PAGE_SIZE))
                .andExpect(status().isOk())
                .andReturn();

        Page<Employee> pagingResult1 = employeeRepository.findAllEmployeesByTeamId(EXIST_TEAM1_ID,PageRequest.of(0,Integer.parseInt(PAGE_SIZE)));


        assertNotNull(pagingResult1);
        assertEquals(EXPECTED_CONTENT_SIZE_IN_FIRST_PAGE, pagingResult1.getContent().size());

        //second page
        mockMvc.perform(get("/api/teams/" + EXIST_TEAM1_ID + "/employees")
                        .param("pageNumber", SECOND_PAGE_NUMBER)
                        .param("pageSize", PAGE_SIZE))
                .andExpect(status().isOk())
                .andReturn();

        Page<Employee> pagingResult2 = employeeRepository.findAllEmployeesByTeamId(EXIST_TEAM1_ID,PageRequest.of(Integer.parseInt(SECOND_PAGE_NUMBER),Integer.parseInt(PAGE_SIZE)));


        assertNotNull(pagingResult2);
        assertEquals(EXPECTED_CONTENT_SIZE_IN_SECOND_PAGE, pagingResult2.getContent().size());
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/get-employees-in-some-team.xml")
    public void testGetAllEmployeesInSomeTeam_whenTeamExists_shouldSuccessAndReturnEmployeesInTeam() throws Exception {
        final Set<String> EXPECTED_EMPLOYEES_NAMES = Set.of("Zaid", "Salim", "Laila", "Hanan", "Umair");
        final  int DEFAULT_PAGE_NUMBER =  0, DEFAULT_PAGE_SIZE = 5;


        mockMvc.perform(get("/api/teams/" + EXIST_TEAM1_ID + "/employees"))
                .andExpect(status().isOk())
                .andReturn();


        Page<Employee> pagingResult = employeeRepository.findAllEmployeesByTeamId(EXIST_TEAM1_ID,PageRequest.of(DEFAULT_PAGE_NUMBER,DEFAULT_PAGE_SIZE));



        List<Employee> employeeResponses = pagingResult.getContent();


        Set<String> actualEmployeesNames = employeeResponses.stream()
                .map(Employee::getName)
                .collect(Collectors.toSet());


        assertNotNull(pagingResult);
        assertNotNull(employeeResponses);
        assertEquals(EXPECTED_EMPLOYEES_NAMES, actualEmployeesNames);
        assertEquals(EXPECTED_EMPLOYEES_NAMES.size(), actualEmployeesNames.size());


    }


    @Test
    @Transactional
    @DatabaseSetup("/dataset/get-employees-in-some-team.xml")
    public void testGetAllEmployeesInSomeTeamWithSortingById_whenTeamExists_shouldSuccessAndReturnEmployeesInTeam() throws Exception {

        final String SORTED_FIELD_BY_ID = "id";


        mockMvc.perform(get("/api/teams/" + EXIST_TEAM1_ID + "/employees")
                        .param("pageNumber", FIRST_PAGE_NUMBER)
                        .param("pageSize", PAGE_SIZE)
                        .param("sortField", SORTED_FIELD_BY_ID)
                        .param("direction", SORT_DIRECTION_DESC))

                .andExpect(status().isOk())
                .andReturn();


        Page<Employee> pagingResult = employeeRepository.findAllEmployeesByTeamId(EXIST_TEAM1_ID, PageRequest.of(0, Integer.parseInt(PAGE_SIZE), Sort.by(Sort.Direction.DESC, SORTED_FIELD_BY_ID)));


        List<Long> descIds = pagingResult.getContent().stream()
                .map(Employee::getId)
                .toList();

        List<Long> sortedDesc = descIds.stream().sorted(Comparator.reverseOrder()).toList();


        assertNotNull(pagingResult);

        assertEquals(sortedDesc, descIds);

    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/get-employees-in-some-team.xml")
    public void testGetAllEmployeesInSomeTeam_whenTeamHasNoEmployees_shouldReturnEmptySet() throws Exception {
        final int EXPECTED_TOTAL_PAGES = 0;
        final int EXPECTED_TOTAL_ELEMENTS = 0;


        mockMvc.perform(get("/api/teams/" + EXIST_EMPTY_TEAM_ID + "/employees")
                        .param("pageNumber", FIRST_PAGE_NUMBER)
                        .param("pageSize", PAGE_SIZE))
                .andExpect(status().isOk())
                .andReturn();

        Page<Employee> pagingResult = employeeRepository.findAllEmployeesByTeamId(EXIST_EMPTY_TEAM_ID, PageRequest.of(0, Integer.parseInt(PAGE_SIZE)));

        assertNotNull(pagingResult);
        assertTrue(pagingResult.getContent().isEmpty());

        assertEquals(EXPECTED_TOTAL_ELEMENTS, pagingResult.getTotalElements());
        assertEquals(EXPECTED_TOTAL_PAGES, pagingResult.getTotalPages());
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
