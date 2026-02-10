package com.example.hrm_system.integration;

import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.entity.Expertise;
import com.example.hrm_system.enums.Gender;
import com.example.hrm_system.repository.EmployeeRepository;
import com.example.hrm_system.repository.ExpertiseRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.hrm_system.enums.ApiError.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ExpertiseRepository expertiseRepository;

    @Test
    @Transactional
    @DatabaseSetup("/dataset/add-employee.xml")
    void testAddEmployeeWithExpertises_shouldCreateEmployeeSuccessfully() throws Exception {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name("Mohamed Abdelrahman")
                .birthDate(LocalDate.of(1980, 8, 5))
                .graduationDate(LocalDate.of(2014, 5, 10))
                .gender(Gender.MALE)
                .grossSalary(70000.0)
                .departmentId(2L)
                .teamId(2L)
                .managerId(1L)
                .build();
        Expertise expertise1 = expertiseRepository.findById(1L).get();
        Expertise expertise2 = expertiseRepository.findById(2L).get();
        employeeRequest.setExpertises(Set.of(expertise1.getName(), expertise2.getName()));

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        EmployeeResponse employeeResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        Employee employee = employeeRepository.findById(employeeResponse.getId()).get();

        assertNotNull(employee);
        assertNotNull(employee.getId());
        assertEquals(employeeRequest.getName(), employee.getName());
        assertEquals(employeeRequest.getBirthDate(), employee.getBirthDate());
        assertEquals(employeeRequest.getGender(), employee.getGender());
        assertEquals(employeeRequest.getGrossSalary(), employee.getGrossSalary());
        assertEquals(employeeRequest.getManagerId(), employee.getManager().getId());
        assertEquals(employeeRequest.getDepartmentId(), employee.getDepartment().getId());
        assertEquals(employeeRequest.getTeamId(), employee.getTeam().getId());
        assertEquals(employeeRequest.getExpertises(), employee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()));
    }

    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    void testAddEmployeeWithoutExpertises_shouldCreateEmployeeSuccessfully() throws Exception {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name("Mohamed Abdelrahman")
                .birthDate(LocalDate.of(1980, 8, 5))
                .graduationDate(LocalDate.of(2014, 5, 10))
                .gender(Gender.MALE)
                .grossSalary(70000.0)
                .managerId(1L)
                .departmentId(2L)
                .teamId(2L)
                .build();

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        EmployeeResponse employeeResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        Employee employee = employeeRepository.findById(employeeResponse.getId()).get();
        assertNotNull(employee);
        assertNotNull(employee.getId());
        assertEquals(employeeRequest.getName(), employee.getName());
        assertEquals(employeeRequest.getBirthDate(), employee.getBirthDate());
        assertEquals(employeeRequest.getGender(), employee.getGender());
        assertEquals(employeeRequest.getGrossSalary(), employee.getGrossSalary());
        assertEquals(employeeRequest.getManagerId(), employee.getManager().getId());
        assertEquals(employeeRequest.getDepartmentId(), employee.getDepartment().getId());
        assertEquals(employeeRequest.getTeamId(), employee.getTeam().getId());
    }

    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    void testAddEmployee_shouldFailWhenNegativeGrossSalary() throws Exception {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name("Malak Ahmed")
                .birthDate(LocalDate.of(1977, 5, 5))
                .graduationDate(LocalDate.of(2000, 6, 27))
                .gender(Gender.FEMALE)
                .grossSalary(-99000.0)
                .managerId(2L)
                .departmentId(2L)
                .teamId(2L)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(INVALID_GROSS_SALARY.getDefaultMessage()))).andReturn();
    }

    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    void testAddEmployee_shouldFailWhenFutureBirthDate() throws Exception {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name("Malak Ahmed")
                .birthDate(LocalDate.of(2029, 5, 5))
                .graduationDate(LocalDate.of(2000, 6, 27))
                .gender(Gender.FEMALE)
                .grossSalary(99000.0)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(INVALID_DATES.getDefaultMessage())));

    }


    @Test
    @DatabaseSetup("/dataset/get-employee-info.xml")
    @Transactional
    void testGetEmployeeInfo_shouldReturnOkWhenFindEmployeeById() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse employeeResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        Employee employee = employeeRepository.findById(employeeResponse.getId()).get();

        assertNotNull(employee);
        assertNotNull(employee.getId());

        assertEquals("Marim Mohamed", employee.getName());
        assertEquals(LocalDate.of(1975, 1, 1), employee.getBirthDate());
        assertEquals(LocalDate.of(2000, 1, 1), employee.getGraduationDate());
        assertEquals(Gender.FEMALE, employee.getGender());
        assertEquals(100000.0, employee.getGrossSalary());
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/get-employee-info.xml")
    void testGetEmployeeInfo_shouldReturnNotFoundWhenEmployeeNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(EMPLOYEE_NOT_FOUND.getDefaultMessage())));
    }
}
