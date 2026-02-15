package com.example.hrm_system.integration;

import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.dto.UpdateEmployeeRequest;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.entity.Expertise;
import com.example.hrm_system.enums.Gender;
import com.example.hrm_system.exception.ApiException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        long countBefore = employeeRepository.count();
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
        long countAfter = employeeRepository.count();

        EmployeeResponse employeeResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertEquals(countBefore + 1, countAfter);

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
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    void testAddEmployee_shouldFailWhenManagerNotFound() throws Exception {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name("Malak Ahmed")
                .birthDate(LocalDate.of(1977, 5, 5))
                .graduationDate(LocalDate.of(2000, 6, 27))
                .gender(Gender.FEMALE)
                .grossSalary(99000.0)
                .managerId(999L)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(MANAGER_NOT_FOUND.getDefaultMessage() + employeeRequest.getManagerId())));

    }

    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    void testAddEmployee_shouldFailWhenDepartmentNotFound() throws Exception {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name("Malak Ahmed")
                .birthDate(LocalDate.of(1977, 5, 5))
                .graduationDate(LocalDate.of(2000, 6, 27))
                .gender(Gender.FEMALE)
                .grossSalary(99000.0)
                .departmentId(99L)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(DEPARTMENT_NOT_FOUND.getDefaultMessage() + employeeRequest.getDepartmentId())));
    }


    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    void testAddEmployee_shouldFailWhenNotFoundTeam() throws Exception {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name("Malak Ahmed")
                .birthDate(LocalDate.of(1977, 5, 5))
                .graduationDate(LocalDate.of(2000, 6, 27))
                .gender(Gender.FEMALE)
                .grossSalary(99000.0)
                .departmentId(2L)
                .managerId(1L)
                .teamId(99L)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(TEAM_NOT_FOUND.getDefaultMessage() + employeeRequest.getTeamId())));
    }

    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    void testAddEmployeeWithExpertises_shouldFailWhenNotFoundExpertises() throws Exception {
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
        employeeRequest.setExpertises(Set.of("Java", "React"));
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(EXPERTISE_NOT_FOUND.getDefaultMessage())));

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


    @Test
    @DatabaseSetup("/dataset/remove-employee.xml")
    @Transactional
    void testDeleteEmployeeWithoutSubordinates_shouldDeleteSuccessfully() throws Exception {
        // 1 Marim -> 2 Ahmed -> 3 Asmaa , 4 Nada
        // we try delete Asmaa (id = 3)
        mockMvc.perform(delete("/api/employees/3")).andExpect(status().isNoContent()).andReturn();

        assertTrue(employeeRepository.findById(3L).isEmpty());
    }

    @Test
    @DatabaseSetup("/dataset/remove-employee.xml")
    @Transactional
    void testDeleteEmployeeHasManagerAndSubordinates_shouldDeleteSuccessfully() throws Exception {
        // 1 Marim -> 2 Ahmed -> 3 Asmaa , 4 Nada
        // we try delete Ahmed (id = 2)
        mockMvc.perform(delete("/api/employees/2")).andExpect(status().isNoContent()).andReturn();

        assertTrue(employeeRepository.findById(2L).isEmpty());

        Employee manager = employeeRepository.findById(1L).get();
        Employee subordinate1 = employeeRepository.findById(3L).get();
        Employee subordinate2 = employeeRepository.findById(4L).get();
        assertEquals(manager.getId(), subordinate1.getManager().getId());
        assertEquals(manager.getId(), subordinate2.getManager().getId());
    }

    @Test
    @DatabaseSetup("/dataset/remove-employee.xml")
    @Transactional
    void testDeleteNotFoundEmployee_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(EMPLOYEE_NOT_FOUND.getDefaultMessage())));
    }

    //delete root manager (marim -> has no manger but has subordinates) -> conflict
    @Test
    @DatabaseSetup("/dataset/remove-employee.xml")
    @Transactional
    void testDeleteRootManager_shouldReturnConflict() throws Exception {
        // 1 Marim -> 2 Ahmed -> 3 Asmaa , 4 Nada
        // we try delete Ahmed (id = 2)
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(INVALID_EMPLOYEE_DELETION.getDefaultMessage())));
    }


    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_shouldReturnOkWhenModifiedEmployee() throws Exception {
        UpdateEmployeeRequest employeeRequest = UpdateEmployeeRequest.builder()
                .name("Zain")
                .birthDate(LocalDate.of(1995, 3, 10))
                .graduationDate(LocalDate.of(2015, 5, 26))
                .gender(Gender.MALE)
                .grossSalary(80000.0)
                .managerId(1L)
                .teamId(1L)
                .departmentId(2L)
                .expertises(Set.of("Java"))
                .build();

        MvcResult result = mockMvc.perform(patch("/api/employees/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeResponse employeeResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertNotNull(employeeResponse);
        assertNotNull(employeeResponse.getId());
        assertEquals(employeeRequest.getName(), employeeResponse.getName());


        Employee updatedEmployee = employeeRepository.findAll()
                .stream()
                .filter(employee -> employee.getName().equals(employeeRequest.getName())
                        && employee.getBirthDate().equals(employeeRequest.getBirthDate()))
                .findFirst()
                .orElseThrow();

        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(employeeRequest.getName(), updatedEmployee.getName());
        assertEquals(employeeRequest.getGrossSalary(), updatedEmployee.getGrossSalary());
        assertEquals(employeeRequest.getGender(), updatedEmployee.getGender());
        assertEquals(employeeRequest.getBirthDate(), updatedEmployee.getBirthDate());
        assertEquals(employeeRequest.getGraduationDate(), updatedEmployee.getGraduationDate());
        assertEquals(employeeRequest.getManagerId(), updatedEmployee.getManager().getId());
        assertEquals(employeeRequest.getTeamId(), updatedEmployee.getTeam().getId());
        assertEquals(employeeRequest.getDepartmentId(), updatedEmployee.getDepartment().getId());
        assertTrue(updatedEmployee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()).contains("Java"));
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_shouldUpdateExpertise() throws Exception {
        UpdateEmployeeRequest employeeRequest = UpdateEmployeeRequest.builder()
                .expertises(Set.of("Java", "Spring Boot"))
                .build();
        Employee employeeBeforeUpdate = employeeRepository.findById(3L)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND));

        MvcResult result = mockMvc.perform(patch("/api/employees/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeResponse employeeResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertTrue(employeeResponse.getExpertises().contains("Spring Boot"));

        Employee updatedEmployee = employeeRepository.findAll()
                .stream()
                .filter(employee -> employee.getName().equals(employeeBeforeUpdate.getName())
                        && employee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()).equals(employeeRequest.getExpertises()))
                .findFirst()
                .orElseThrow();

        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(employeeBeforeUpdate.getId(),updatedEmployee.getId());
        assertEquals(employeeBeforeUpdate.getName(), updatedEmployee.getName());
        assertTrue(updatedEmployee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()).contains("Spring Boot"));
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_shouldReturnBadRequestWhenDepartmentIsInvalid() throws Exception {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name("Zain")
                .birthDate(LocalDate.of(1995, 3, 10))
                .graduationDate(LocalDate.of(2015, 5, 26))
                .gender(Gender.MALE)
                .grossSalary(80000.0)
                .managerId(1L)
                .teamId(1L)
                .departmentId(999L)
                .expertises(Set.of("Java"))
                .build();

        mockMvc.perform(patch("/api/employees/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                .contains(DEPARTMENT_NOT_FOUND.getDefaultMessage())));
    }
    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_shouldReturnBadRequest_whenEmployeeAssignedAsOwnManager() throws Exception {

        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .managerId(1L)
                .build();

        mockMvc.perform(patch("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(INVALID_MANAGER.getDefaultMessage())));
    }


}
