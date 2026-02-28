package com.example.hrm_system.integration;

import com.example.hrm_system.configuration.JacksonConfiguration;
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
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.hrm_system.enums.ApiError.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    private static final Gender FEMALE_EMPLOYEE = Gender.FEMALE;
    private static final Gender MALE_EMPLOYEE = Gender.MALE;
    private static final Long NO_EXIST_EMPLOYEE_ID = 99L;
    private static final Long NO_EXIST_MANAGER_ID = 99L;
    private static final Long NO_EXIST_TEAM_ID = 99L;
    private static final Long NO_EXIST_DEPARTMENT_ID = 99L;
    private static final Long EXIST_MANAGER_ID = 1L;
    private static final Long EXIST_DEPARTMENT1_ID = 1L;
    private static final Long EXIST_DEPARTMENT2_ID = 2L;
    private static final Long EXIST_TEAM1_ID = 1L;
    private static final Long EXIST_TEAM2_ID = 2L;
    private static final Long EXIST_EXPERTISE1_ID = 1L;
    private static final Long EXIST_EXPERTISE2_ID = 2L;
    private static final Long EXIST_EMPLOYEE2_ID = 2L;
    private static final Long EXIST_EMPLOYEE3_ID = 3L;
    private static final Long EXIST_EMPLOYEE4_ID = 4L;


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JacksonConfiguration jacksonConfiguration;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ExpertiseRepository expertiseRepository;

    @Test
    @Transactional
    @DatabaseSetup("/dataset/add-employee.xml")
    public void testAddEmployeeWithExpertises_whenEnterValidData_shouldCreateEmployeeSuccessfully() throws Exception {
        long countBefore = employeeRepository.count();

        final String EMPLOYEE_NAME = "Mohamed Abdelrahman";
        final LocalDate EMPLOYEE_BIRTH_DATE = LocalDate.of(1980, 8, 5);
        final LocalDate EMPLOYEE_GRADUATION_DATE = LocalDate.of(2014, 5, 10);
        final BigDecimal EMPLOYEE_GROSS_SALARY = BigDecimal.valueOf(70000);


        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name(EMPLOYEE_NAME)
                .birthDate(EMPLOYEE_BIRTH_DATE)
                .graduationDate(EMPLOYEE_GRADUATION_DATE)
                .gender(MALE_EMPLOYEE)
                .grossSalary(EMPLOYEE_GROSS_SALARY)
                .departmentId(EXIST_DEPARTMENT2_ID)
                .teamId(EXIST_TEAM2_ID)
                .managerId(EXIST_MANAGER_ID)
                .build();
        Expertise expertise1 = expertiseRepository.findById(EXIST_EXPERTISE1_ID).get();
        Expertise expertise2 = expertiseRepository.findById(EXIST_EXPERTISE2_ID).get();
        final Set<String> EXPERTISES = Set.of(expertise1.getName(), expertise2.getName());
        employeeRequest.setExpertises(EXPERTISES);

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        EmployeeResponse employeeResponse = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        long countAfter = employeeRepository.count();
        assertEquals(countBefore + 1, countAfter);

        Employee employee = employeeRepository.findById(employeeResponse.getId()).get();
        assertNotNull(employee);
        assertNotNull(employee.getId());
        assertEquals(employee.getId(), employeeResponse.getId());
        assertEquals(employee.getName(), EMPLOYEE_NAME);
        assertEquals(employee.getBirthDate(), EMPLOYEE_BIRTH_DATE);
        assertEquals(employee.getGraduationDate(), EMPLOYEE_GRADUATION_DATE);
        assertEquals(employee.getGender(), MALE_EMPLOYEE);
        assertEquals(employee.getGrossSalary(), EMPLOYEE_GROSS_SALARY);
        assertEquals(employee.getManager().getId(), EXIST_MANAGER_ID);
        assertEquals(employee.getDepartment().getId(), EXIST_DEPARTMENT2_ID);
        assertEquals(employee.getTeam().getId(), EXIST_TEAM2_ID);
        assertEquals(employee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()), EXPERTISES);
    }

    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    public void testAddEmployeeWithoutExpertises_whenEnterValidData_shouldCreateEmployeeSuccessfully() throws Exception {
        long countBefore = employeeRepository.count();

        final String EMPLOYEE_NAME = "Mohamed Abdelrahman";
        final LocalDate EMPLOYEE_BIRTH_DATE = LocalDate.of(1980, 8, 5);
        final LocalDate EMPLOYEE_GRADUATION_DATE = LocalDate.of(2014, 5, 10);
        final BigDecimal EMPLOYEE_GROSS_SALARY = BigDecimal.valueOf(70000.0);


        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name(EMPLOYEE_NAME)
                .birthDate(EMPLOYEE_BIRTH_DATE)
                .graduationDate(EMPLOYEE_GRADUATION_DATE)
                .gender(MALE_EMPLOYEE)
                .grossSalary(EMPLOYEE_GROSS_SALARY)
                .departmentId(EXIST_DEPARTMENT2_ID)
                .teamId(EXIST_TEAM2_ID)
                .managerId(EXIST_MANAGER_ID)
                .build();
        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        long countAfter = employeeRepository.count();

        EmployeeResponse employeeResponse = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertEquals(countBefore + 1, countAfter);

        Employee employee = employeeRepository.findById(employeeResponse.getId()).get();
        assertNotNull(employee);
        assertNotNull(employee.getId());
        assertEquals(employee.getId(), employeeResponse.getId());
        assertEquals(employee.getName(), EMPLOYEE_NAME);
        assertEquals(employee.getBirthDate(), EMPLOYEE_BIRTH_DATE);
        assertEquals(employee.getGraduationDate(), EMPLOYEE_GRADUATION_DATE);
        assertEquals(employee.getGender(), MALE_EMPLOYEE);
        assertEquals(employee.getGrossSalary(), EMPLOYEE_GROSS_SALARY);
        assertEquals(employee.getManager().getId(), EXIST_MANAGER_ID);
        assertEquals(employee.getDepartment().getId(), EXIST_DEPARTMENT2_ID);
        assertEquals(employee.getTeam().getId(), EXIST_TEAM2_ID);
    }

    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    public void testAddEmployee_whenEnterInvalidGrossSalary_shouldReturnBadRequest() throws Exception {
        final String EMPLOYEE_NAME = "Mohamed Abdelrahman";
        final LocalDate EMPLOYEE_BIRTH_DATE = LocalDate.of(1980, 8, 5);
        final LocalDate EMPLOYEE_GRADUATION_DATE = LocalDate.of(2014, 5, 10);
        final BigDecimal NEGATIVE_GROSS_SALARY = BigDecimal.valueOf(-70000.0); //Invalid gross salary

        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name(EMPLOYEE_NAME)
                .birthDate(EMPLOYEE_BIRTH_DATE)
                .graduationDate(EMPLOYEE_GRADUATION_DATE)
                .gender(MALE_EMPLOYEE)
                .grossSalary(NEGATIVE_GROSS_SALARY)
                .departmentId(EXIST_DEPARTMENT2_ID)
                .teamId(EXIST_TEAM1_ID)
                .managerId(EXIST_MANAGER_ID)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(INVALID_GROSS_SALARY.getDefaultMessage()))).andReturn();
    }

    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    public void testAddEmployee_whenEnterFutureBirthDate_shouldReturnBadRequest() throws Exception {
        final String EMPLOYEE_NAME = "Mohamed Abdelrahman";
        final LocalDate INVALID_BIRTH_DATE = LocalDate.of(2027, 8, 5); // Invalid birthDate
        final LocalDate EMPLOYEE_GRADUATION_DATE = LocalDate.of(2014, 5, 10);
        final BigDecimal EMPLOYEE_GROSS_SALARY = BigDecimal.valueOf(70000.0);


        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name(EMPLOYEE_NAME)
                .birthDate(INVALID_BIRTH_DATE)
                .graduationDate(EMPLOYEE_GRADUATION_DATE)
                .gender(MALE_EMPLOYEE)
                .grossSalary(EMPLOYEE_GROSS_SALARY)
                .departmentId(EXIST_DEPARTMENT2_ID)
                .teamId(EXIST_TEAM2_ID)
                .managerId(EXIST_MANAGER_ID)
                .build();
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(INVALID_DATES.getDefaultMessage())));

    }


    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    public void testAddEmployee_whenEnterNotFoundManager_shouldReturnNotFound() throws Exception {
        final String EMPLOYEE_NAME = "Malak Ziad";
        final LocalDate EMPLOYEE_BIRTH_DATE = LocalDate.of(2003, 3, 14);
        final LocalDate EMPLOYEE_GRADUATION_DATE = LocalDate.of(2024, 5, 30);
        final BigDecimal EMPLOYEE_GROSS_SALARY = BigDecimal.valueOf(90000.0);

        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name(EMPLOYEE_NAME)
                .birthDate(EMPLOYEE_BIRTH_DATE)
                .graduationDate(EMPLOYEE_GRADUATION_DATE)
                .gender(FEMALE_EMPLOYEE)
                .grossSalary(EMPLOYEE_GROSS_SALARY)
                .managerId(NO_EXIST_MANAGER_ID) //Invalid manager
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(MANAGER_NOT_FOUND.getDefaultMessage() + employeeRequest.getManagerId())));

    }

    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    public void testAddEmployee_whenNotFoundDepartment_shouldReturnNotFound() throws Exception {
        final String EMPLOYEE_NAME = "Mohamed";
        final LocalDate EMPLOYEE_BIRTH_DATE = LocalDate.of(1980, 8, 5);
        final LocalDate EMPLOYEE_GRADUATION_DATE = LocalDate.of(2014, 5, 10);
        final BigDecimal EMPLOYEE_GROSS_SALARY = BigDecimal.valueOf(70000.0);


        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name(EMPLOYEE_NAME)
                .birthDate(EMPLOYEE_BIRTH_DATE)
                .graduationDate(EMPLOYEE_GRADUATION_DATE)
                .gender(MALE_EMPLOYEE)
                .grossSalary(EMPLOYEE_GROSS_SALARY)
                .departmentId(NO_EXIST_DEPARTMENT_ID) //Invalid department
                .teamId(EXIST_TEAM2_ID)
                .managerId(EXIST_MANAGER_ID)
                .build();
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(DEPARTMENT_NOT_FOUND.getDefaultMessage() + employeeRequest.getDepartmentId())));
    }


    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    public void testAddEmployee_whenNotFoundTeam_shouldReturnNotFound() throws Exception {
        final String EMPLOYEE_NAME = "Malak Ziad";
        final LocalDate EMPLOYEE_BIRTH_DATE = LocalDate.of(2003, 3, 14);
        final LocalDate EMPLOYEE_GRADUATION_DATE = LocalDate.of(2024, 5, 30);
        final BigDecimal EMPLOYEE_GROSS_SALARY = BigDecimal.valueOf(90000.0);

        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name(EMPLOYEE_NAME)
                .birthDate(EMPLOYEE_BIRTH_DATE)
                .graduationDate(EMPLOYEE_GRADUATION_DATE)
                .gender(FEMALE_EMPLOYEE)
                .grossSalary(EMPLOYEE_GROSS_SALARY)
                .managerId(EXIST_MANAGER_ID)
                .teamId(NO_EXIST_TEAM_ID)   //Invalid team
                .departmentId(EXIST_DEPARTMENT1_ID)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(TEAM_NOT_FOUND.getDefaultMessage() + employeeRequest.getTeamId())));
    }

    @Test
    @DatabaseSetup("/dataset/add-employee.xml")
    @Transactional
    public void testAddEmployeeWithExpertises_whenNotFoundExpertises_shouldReturnNotFound() throws Exception {
        final String EMPLOYEE_NAME = "Mohamed Abdelrahman";
        final LocalDate EMPLOYEE_BIRTH_DATE = LocalDate.of(1980, 8, 5);
        final LocalDate EMPLOYEE_GRADUATION_DATE = LocalDate.of(2014, 5, 10);
        final BigDecimal EMPLOYEE_GROSS_SALARY = BigDecimal.valueOf(70000.0);
        final Set<String> NOT_FOUND_EXPERTISES = Set.of("DataBase", "React"); //Invalid expertises

        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .name(EMPLOYEE_NAME)
                .birthDate(EMPLOYEE_BIRTH_DATE)
                .graduationDate(EMPLOYEE_GRADUATION_DATE)
                .gender(MALE_EMPLOYEE)
                .grossSalary(EMPLOYEE_GROSS_SALARY)
                .departmentId(EXIST_DEPARTMENT2_ID)
                .teamId(EXIST_TEAM2_ID)
                .managerId(EXIST_MANAGER_ID)
                .build();
        employeeRequest.setExpertises(NOT_FOUND_EXPERTISES);
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(EXPERTISE_NOT_FOUND.getDefaultMessage())));

    }

    @Test
    @DatabaseSetup("/dataset/get-employee-info.xml")
    @Transactional
    public void testGetEmployeeInfo_whenFindEmployeeById_shouldReturnOk() throws Exception {

        final String EXIST_EMPLOYEE_NAME = "Marim Mohamed";
        final LocalDate EXIST_EMPLOYEE_BIRTH_DATE = LocalDate.of(1975, 1, 1);
        final LocalDate EXIST_EMPLOYEE_GRAD_DATE = LocalDate.of(2000, 1, 1);
        final BigDecimal EXIST_EMPLOYEE_GROSS_SALARY = new BigDecimal("100000.00");

        MvcResult result = mockMvc.perform(get("/api/employees/" + EXIST_MANAGER_ID))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse employeeResponse = jacksonConfiguration.objectMapper()
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        Employee employee = employeeRepository.findById(employeeResponse.getId()).get();

        assertNotNull(employee);
        assertNotNull(employee.getId());
        assertEquals(employee.getId(), employeeResponse.getId());
        assertEquals(EXIST_EMPLOYEE_NAME, employee.getName());
        assertEquals(EXIST_EMPLOYEE_BIRTH_DATE, employee.getBirthDate());
        assertEquals(EXIST_EMPLOYEE_GRAD_DATE, employee.getGraduationDate());
        assertEquals(FEMALE_EMPLOYEE, employee.getGender());
        assertEquals(EXIST_EMPLOYEE_GROSS_SALARY, employee.getGrossSalary());
        assertEquals(EXIST_DEPARTMENT1_ID, employee.getDepartment().getId());
        assertEquals(EXIST_TEAM1_ID, employee.getTeam().getId());
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/get-employee-info.xml")
    public void testGetEmployeeInfo_whenNotFoundEmployee_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/" + NO_EXIST_EMPLOYEE_ID))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(EMPLOYEE_NOT_FOUND.getDefaultMessage())));
    }


    @Test
    @DatabaseSetup("/dataset/remove-employee.xml")
    @Transactional
    public void testDeleteEmployeeWithoutSubordinates_whenSpecifyValidEmployeeId_shouldDeleteSuccessfully() throws Exception {
        // 1 Marim -> 2 Ahmed -> 3 Asmaa , 4 Nada
        // we try delete Asmaa (id = 3)
        mockMvc.perform(delete("/api/employees/" + EXIST_EMPLOYEE3_ID))
                .andExpect(status().isNoContent()).andReturn();

        assertTrue(employeeRepository.findById(EXIST_EMPLOYEE3_ID).isEmpty());
    }

    @Test
    @DatabaseSetup("/dataset/remove-employee.xml")
    @Transactional
    public void testDeleteEmployeeHasManagerAndSubordinates_whenSpecifyValidEmployeeId_shouldDeleteSuccessfully() throws Exception {
        // 1 Marim -> 2 Ahmed -> 3 Asmaa , 4 Nada
        // we try delete Ahmed (id = 2)
        mockMvc.perform(delete("/api/employees/" + EXIST_EMPLOYEE2_ID))
                .andExpect(status().isNoContent()).andReturn();

        assertTrue(employeeRepository.findById(EXIST_EMPLOYEE2_ID).isEmpty());

        Employee manager = employeeRepository.findById(EXIST_MANAGER_ID).get();
        Employee subordinate1 = employeeRepository.findById(EXIST_EMPLOYEE3_ID).get();
        Employee subordinate2 = employeeRepository.findById(EXIST_EMPLOYEE4_ID).get();
        assertEquals(manager.getId(), subordinate1.getManager().getId());
        assertEquals(manager.getId(), subordinate2.getManager().getId());
    }

    @Test
    @DatabaseSetup("/dataset/remove-employee.xml")
    @Transactional
    public void testDeleteNotFoundEmployee_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/employees/" + NO_EXIST_EMPLOYEE_ID))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(EMPLOYEE_NOT_FOUND.getDefaultMessage())));
    }

    //delete root manager (marim -> has no manger but has subordinates) -> conflict
    @Test
    @DatabaseSetup("/dataset/remove-employee.xml")
    @Transactional
    public void testDeleteRootManager_whenRemoveManagerHasNoManagerAndHasSubordinates_shouldReturnConflict() throws Exception {
        // 1 Marim -> 2 Ahmed -> 3 Asmaa , 4 Nada
        // we try delete Ahmed (id = 2)
        mockMvc.perform(delete("/api/employees/" + EXIST_MANAGER_ID))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(INVALID_EMPLOYEE_DELETION.getDefaultMessage())));
    }


    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenEnterValidData_shouldReturnOk() throws Exception {

        final String UPDATE_NAME = "Zain";
        final LocalDate UPDATE_BIRTH_DATE = LocalDate.of(1995, 3, 10);
        final LocalDate UPDATE_GRAD_DATE = LocalDate.of(2015, 5, 26);
        final BigDecimal UPDATE_GROSS_SALARY = BigDecimal.valueOf(80000.0);
        final Set<String> UPDATE_EXPERTISES = Set.of("Java");


        UpdateEmployeeRequest employeeRequest = UpdateEmployeeRequest.builder()
                .name(JsonNullable.of(UPDATE_NAME))
                .birthDate(JsonNullable.of(UPDATE_BIRTH_DATE))
                .graduationDate(JsonNullable.of(UPDATE_GRAD_DATE))
                .gender(JsonNullable.of(MALE_EMPLOYEE))
                .grossSalary(JsonNullable.of(UPDATE_GROSS_SALARY))
                .expertises(JsonNullable.of(UPDATE_EXPERTISES))
                .managerId(JsonNullable.of(EXIST_MANAGER_ID))
                .departmentId(JsonNullable.of(EXIST_DEPARTMENT2_ID))
                .teamId(JsonNullable.of(EXIST_TEAM1_ID))
                .build();


        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE3_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeResponse employeeResponse = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        Employee updatedEmployee = employeeRepository.findById(EXIST_EMPLOYEE3_ID).orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                EMPLOYEE_NOT_FOUND.getDefaultMessage()));


        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(employeeResponse.getId(), EXIST_EMPLOYEE3_ID);
        assertEquals(updatedEmployee.getId(), EXIST_EMPLOYEE3_ID);
        assertEquals(updatedEmployee.getName(), UPDATE_NAME);
        assertEquals(updatedEmployee.getGrossSalary(), UPDATE_GROSS_SALARY);
        assertEquals(updatedEmployee.getGender(), MALE_EMPLOYEE);
        assertEquals(updatedEmployee.getBirthDate(), UPDATE_BIRTH_DATE);
        assertEquals(updatedEmployee.getGraduationDate(), UPDATE_GRAD_DATE);
        assertEquals(updatedEmployee.getManager().getId(), EXIST_MANAGER_ID);
        assertEquals(updatedEmployee.getTeam().getId(), EXIST_TEAM1_ID);
        assertEquals(updatedEmployee.getDepartment().getId(), EXIST_DEPARTMENT2_ID);
        assertEquals(updatedEmployee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()), UPDATE_EXPERTISES);
        assertTrue(updatedEmployee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()).contains("Java"));
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenEnterValidExpertises_shouldUpdateExpertise() throws Exception {
        final Set<String> UPDATE_EXPERTISES = Set.of("Java", "Spring Boot");

        UpdateEmployeeRequest employeeRequest = UpdateEmployeeRequest.builder()
                .expertises(JsonNullable.of(UPDATE_EXPERTISES))
                .build();

        mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE3_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Employee updatedEmployee = employeeRepository.findById(EXIST_EMPLOYEE3_ID).orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                EMPLOYEE_NOT_FOUND.getDefaultMessage()));

        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(updatedEmployee.getId(), EXIST_EMPLOYEE3_ID);
        assertEquals(updatedEmployee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()), UPDATE_EXPERTISES);
    }


    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenEnterValidBirthDateAndGraduationDate_shouldUpdateBirthDateAndGraduationDate() throws Exception {
        final LocalDate UPDATE_BIRTH_DATE = LocalDate.of(2002, 5, 1);
        final LocalDate UPDATE_GRADUATION_DATE = LocalDate.of(2024, 6, 15);

        UpdateEmployeeRequest employeeRequest = UpdateEmployeeRequest.builder()
                .birthDate(JsonNullable.of(UPDATE_BIRTH_DATE))
                .graduationDate(JsonNullable.of(UPDATE_GRADUATION_DATE))
                .build();


        mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE2_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();


        Employee updatedEmployee = employeeRepository.findById(EXIST_EMPLOYEE2_ID).orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                EMPLOYEE_NOT_FOUND.getDefaultMessage()));

        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(updatedEmployee.getId(), EXIST_EMPLOYEE2_ID);
        assertEquals(updatedEmployee.getBirthDate(), UPDATE_BIRTH_DATE);
        assertEquals(updatedEmployee.getGraduationDate(), UPDATE_GRADUATION_DATE);
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenDepartmentIsInvalid_shouldReturnNotFound() throws Exception {

        UpdateEmployeeRequest employeeRequest = UpdateEmployeeRequest.builder()
                .departmentId(JsonNullable.of(NO_EXIST_DEPARTMENT_ID))
                .build();


        mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE3_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(DEPARTMENT_NOT_FOUND.getDefaultMessage())));
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenEmployeeAssignedAsOwnManager_shouldReturnBadRequest() throws Exception {

        UpdateEmployeeRequest employeeRequest = UpdateEmployeeRequest.builder()
                .managerId(JsonNullable.of(EXIST_MANAGER_ID))
                .build();

        mockMvc.perform(patch("/api/employees/" + EXIST_MANAGER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(INVALID_MANAGER.getDefaultMessage())));
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenNotFoundEmployee_shouldReturnNotFound() throws Exception {

        final String UPDATE_NAME = "Reem";
        final LocalDate UPDATE_BIRTH_DATE = LocalDate.of(1995, 3, 10);
        final LocalDate UPDATE_GRAD_DATE = LocalDate.of(2015, 5, 26);
        final BigDecimal UPDATE_GROSS_SALARY = BigDecimal.valueOf(80000.0);
        final Set<String> UPDATE_EXPERTISES = Set.of("Java");


        UpdateEmployeeRequest employeeRequest = UpdateEmployeeRequest.builder()
                .name(JsonNullable.of(UPDATE_NAME))
                .birthDate(JsonNullable.of(UPDATE_BIRTH_DATE))
                .graduationDate(JsonNullable.of(UPDATE_GRAD_DATE))
                .gender(JsonNullable.of(FEMALE_EMPLOYEE))
                .grossSalary(JsonNullable.of(UPDATE_GROSS_SALARY))
                .managerId(JsonNullable.of(EXIST_MANAGER_ID))
                .departmentId(JsonNullable.of(EXIST_DEPARTMENT2_ID))
                .teamId(JsonNullable.of(EXIST_TEAM2_ID))
                .expertises(JsonNullable.of(UPDATE_EXPERTISES))
                .build();

        mockMvc.perform(patch("/api/employees/" + NO_EXIST_EMPLOYEE_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(EMPLOYEE_NOT_FOUND.getDefaultMessage())));
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenAllFieldAcceptNullValue_shouldReturnOk() throws Exception {

        UpdateEmployeeRequest employeeRequest = UpdateEmployeeRequest.builder()
                .name(JsonNullable.of(null))
                .birthDate(JsonNullable.of(null))
                .graduationDate(JsonNullable.of(null))
                .gender(JsonNullable.of(null))
                .grossSalary(JsonNullable.of(null))
                .expertises(JsonNullable.of(null))
                .managerId(JsonNullable.of(null))
                .departmentId(JsonNullable.of(null))
                .teamId(JsonNullable.of(null))
                .build();

        mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE2_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();


        Employee updatedEmployee = employeeRepository.findById(EXIST_EMPLOYEE2_ID).orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                EMPLOYEE_NOT_FOUND.getDefaultMessage()));


        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(EXIST_EMPLOYEE2_ID, updatedEmployee.getId());
        assertNull(updatedEmployee.getName());
        assertNull(updatedEmployee.getBirthDate());
        assertNull(updatedEmployee.getGraduationDate());
        assertNull(updatedEmployee.getGrossSalary());
        assertNull(updatedEmployee.getGender());
        assertNull(updatedEmployee.getManager());
        assertNull(updatedEmployee.getDepartment());
        assertNull(updatedEmployee.getTeam());
        assertNull(updatedEmployee.getExpertises());
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenNameFieldAcceptNullValue_shouldReturnOk() throws Exception {

        UpdateEmployeeRequest employeeRequest = UpdateEmployeeRequest.builder()
                .name(JsonNullable.of(null))
                .build();

        mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE2_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Employee updatedEmployee = employeeRepository.findById(EXIST_EMPLOYEE2_ID).orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND,
                EMPLOYEE_NOT_FOUND.getDefaultMessage()));


        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(updatedEmployee.getId(), EXIST_EMPLOYEE2_ID);
        assertThat(updatedEmployee.getName()).isNull();
    }
}
