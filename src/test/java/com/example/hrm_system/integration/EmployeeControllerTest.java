package com.example.hrm_system.integration;

import com.example.hrm_system.configuration.JacksonConfiguration;
import com.example.hrm_system.dto.EmployeeRequest;
import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.dto.EmployeeSalary;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
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

    private static final BigDecimal TAX_RATIO = BigDecimal.valueOf(0.15);
    private static final BigDecimal TAX_REMAINDER = BigDecimal.ONE.subtract(TAX_RATIO);
    private static final BigDecimal INSURANCE_AMOUNT = BigDecimal.valueOf(500);


    private static final String NAME = "name", BIRTH_DATE = "birthDate", GENDER = "gender", GRADUATION_DATE = "graduationDate", GROSS_SALARY = "grossSalary", EXPERTISES = "expertises", DEPARTMENT_ID = "departmentId", MANAGER_ID = "managerId", TEAM_ID = "teamId";


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
        employeeRequest.setExpertises(Set.of(expertise1.getName(), expertise2.getName()));

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        EmployeeResponse employeeResponse = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        Employee employee = employeeRepository.findById(employeeResponse.getId()).get();

        assertNotNull(employee);
        assertNotNull(employee.getId());
        assertEquals(employee.getId(), employeeResponse.getId());
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


        Map<String, Object> employeeRequest = new HashMap<>();
        employeeRequest.put(NAME, UPDATE_NAME);
        employeeRequest.put(BIRTH_DATE, UPDATE_BIRTH_DATE);
        employeeRequest.put(GRADUATION_DATE, UPDATE_GRAD_DATE);
        employeeRequest.put(GENDER, MALE_EMPLOYEE);
        employeeRequest.put(GROSS_SALARY, UPDATE_GROSS_SALARY);
        employeeRequest.put(EXPERTISES, UPDATE_EXPERTISES);
        employeeRequest.put(MANAGER_ID, EXIST_MANAGER_ID);
        employeeRequest.put(DEPARTMENT_ID, EXIST_DEPARTMENT2_ID);
        employeeRequest.put(TEAM_ID, EXIST_TEAM1_ID);

        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE3_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeResponse employeeResponse = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(employeeResponse);
        assertNotNull(employeeResponse.getId());
        Employee updatedEmployee = employeeRepository.findAll()
                .stream()
                .filter(employee -> employee.getId().equals(employeeResponse.getId())
                        && employee.getName().equals(employeeRequest.get(NAME))
                        && employee.getBirthDate().equals(employeeRequest.get(BIRTH_DATE)))
                .findFirst()
                .orElseThrow();

        assertNotNull(updatedEmployee);
        assertEquals(employeeResponse.getId(), updatedEmployee.getId());
        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(employeeResponse.getId(), updatedEmployee.getId());
        assertEquals(employeeRequest.get(NAME), updatedEmployee.getName());
        assertEquals(employeeRequest.get(GROSS_SALARY), updatedEmployee.getGrossSalary());
        assertEquals(employeeRequest.get(GENDER), updatedEmployee.getGender());
        assertEquals(employeeRequest.get(BIRTH_DATE), updatedEmployee.getBirthDate());
        assertEquals(employeeRequest.get(GRADUATION_DATE), updatedEmployee.getGraduationDate());
        assertEquals(employeeRequest.get(MANAGER_ID), updatedEmployee.getManager().getId());
        assertEquals(employeeRequest.get(TEAM_ID), updatedEmployee.getTeam().getId());
        assertEquals(employeeRequest.get(DEPARTMENT_ID), updatedEmployee.getDepartment().getId());
        assertEquals(employeeRequest.get(EXPERTISES), updatedEmployee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()));
        assertTrue(updatedEmployee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()).contains("Java"));
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenEnterValidExpertises_shouldUpdateExpertise() throws Exception {
        final Set<String> UPDATE_EXPERTISES = Set.of("Java", "Spring Boot");

        Map<String, Object> employeeRequest = new HashMap<>();
        employeeRequest.put(EXPERTISES, UPDATE_EXPERTISES);

        Employee employeeBeforeUpdate = employeeRepository.findById(EXIST_EMPLOYEE3_ID)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND));

        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE3_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeResponse employeeResponse = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertEquals(employeeResponse.getExpertises(), employeeRequest.get(EXPERTISES));

        Employee updatedEmployee = employeeRepository.findAll()
                .stream()
                .filter(employee -> employee.getId().equals(employeeResponse.getId())
                        && employee.getExpertises().stream().map(Expertise::getName).collect(Collectors.toSet()).equals(employeeRequest.get(EXPERTISES)))
                .findFirst()
                .orElseThrow();

        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(employeeBeforeUpdate.getId(), updatedEmployee.getId());
        assertEquals(updatedEmployee.getExpertises(), employeeBeforeUpdate.getExpertises());
    }


    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenEnterValidBirthDateAndGraduationDate_shouldUpdateBirthDateAndGraduationDate() throws Exception {
        final LocalDate UPDATE_BIRTH_DATE = LocalDate.of(2002, 5, 1);
        final LocalDate UPDATE_GRADUATION_DATE = LocalDate.of(2024, 6, 15);

        Map<String, Object> employeeRequest = new HashMap<>();
        employeeRequest.put(BIRTH_DATE, UPDATE_BIRTH_DATE);
        employeeRequest.put(GRADUATION_DATE, UPDATE_GRADUATION_DATE);

        Employee employeeBeforeUpdate = employeeRepository.findById(EXIST_EMPLOYEE2_ID)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND));

        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE2_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeResponse employeeResponse = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);


        Employee updatedEmployee = employeeRepository.findAll()
                .stream()
                .filter(employee -> employee.getId().equals(employeeResponse.getId())
                        && employee.getGraduationDate().equals(employeeRequest.get(GRADUATION_DATE))
                        && employee.getBirthDate().equals(employeeRequest.get(BIRTH_DATE)))
                .findFirst()
                .orElseThrow();

        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(employeeBeforeUpdate.getId(), updatedEmployee.getId());
        assertEquals(employeeRequest.get(BIRTH_DATE), updatedEmployee.getBirthDate());
        assertEquals(employeeRequest.get(GRADUATION_DATE), updatedEmployee.getGraduationDate());
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenDepartmentIsInvalid_shouldReturnNotFound() throws Exception {
        Map<String, Object> employeeRequest = new HashMap<>();
        employeeRequest.put(DEPARTMENT_ID, NO_EXIST_DEPARTMENT_ID);

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
        Map<String, Object> employeeRequest = new HashMap<>();
        employeeRequest.put(MANAGER_ID, EXIST_MANAGER_ID);
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
    public void testModifyEmployee_whenEmployeeIsInvalid_shouldReturnNotFound() throws Exception {

        final String UPDATE_NAME = "Reem";
        final LocalDate UPDATE_BIRTH_DATE = LocalDate.of(1995, 3, 10);
        final LocalDate UPDATE_GRAD_DATE = LocalDate.of(2015, 5, 26);
        final BigDecimal UPDATE_GROSS_SALARY = BigDecimal.valueOf(80000.0);
        final Set<String> UPDATE_EXPERTISES = Set.of("Java");


        Map<String, Object> employeeRequest = new HashMap<>();
        employeeRequest.put(NAME, UPDATE_NAME);
        employeeRequest.put(BIRTH_DATE, UPDATE_BIRTH_DATE);
        employeeRequest.put(GRADUATION_DATE, UPDATE_GRAD_DATE);
        employeeRequest.put(GENDER, FEMALE_EMPLOYEE);
        employeeRequest.put(GROSS_SALARY, UPDATE_GROSS_SALARY);
        employeeRequest.put(MANAGER_ID, EXIST_MANAGER_ID);
        employeeRequest.put(TEAM_ID, EXIST_TEAM1_ID);
        employeeRequest.put(DEPARTMENT_ID, EXIST_DEPARTMENT2_ID);
        employeeRequest.put(EXPERTISES, UPDATE_EXPERTISES);

        mockMvc.perform(patch("/api/employees/" + NO_EXIST_EMPLOYEE_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains(EMPLOYEE_NOT_FOUND.getDefaultMessage())));
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/modify-employee.xml")
    public void testModifyEmployee_whenSetFieldsNull_shouldReturnOk() throws Exception {

        Map<String, Object> employeeRequest = new HashMap<>();
        employeeRequest.put(NAME, null);
        employeeRequest.put(BIRTH_DATE, null);
        employeeRequest.put(GRADUATION_DATE, null);
        employeeRequest.put(GROSS_SALARY, null);
        employeeRequest.put(GENDER, null);
        employeeRequest.put(MANAGER_ID, null);
        employeeRequest.put(DEPARTMENT_ID, null);
        employeeRequest.put(TEAM_ID, null);
        employeeRequest.put(EXPERTISES, null);

        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE2_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeResponse employeeResponse = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(employeeResponse);
        assertNotNull(employeeResponse.getId());

        Employee updatedEmployee = employeeRepository.findAll()
                .stream()
                .filter(employee -> employee.getId().equals(employeeResponse.getId()))
                .findFirst()
                .orElseThrow();


        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(employeeResponse.getId(), updatedEmployee.getId());
        assertNotNull(updatedEmployee.getName());
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
    public void testModifyEmployee_whenNameNullable_shouldReturnOkIgnoreChanges() throws Exception {

        Map<String, Object> employeeRequest = new HashMap<>();
        employeeRequest.put(NAME, null);

        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXIST_EMPLOYEE2_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonConfiguration.objectMapper().writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeResponse employeeResponse = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(employeeResponse);
        assertNotNull(employeeResponse.getId());
        Employee updatedEmployee = employeeRepository.findById(EXIST_EMPLOYEE2_ID).get();

        assertNotNull(updatedEmployee);
        assertNotNull(updatedEmployee.getId());
        assertEquals(employeeResponse.getId(), updatedEmployee.getId());
        assertEquals(employeeResponse.getName(), updatedEmployee.getName());
    }

    @Test
    @Transactional
    @DatabaseSetup("/dataset/get-employee-salary.xml")
    public void testGetEmployeeSalary_whenEmployeeExistsAndValidGrossSalary_shouldReturnCorrectNetSalary() throws Exception {
        final BigDecimal grossSalary = BigDecimal.valueOf(15000);

        /* net = grossSalary - (grossSalary*TAX_RATIO) - INSURANCE_AMOUNT
                = grossSalary(1-TAX_RATIO)-INSURANCE_AMOUNT
              = grossSalary(TAX_REMAINDER)-INSURANCE_AMOUNT */
        BigDecimal netSalary = grossSalary.multiply(TAX_REMAINDER).subtract(INSURANCE_AMOUNT);

        MvcResult result = mockMvc.perform(get("/api/employees/" + EXIST_EMPLOYEE2_ID + "/salary"))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeSalary employeeSalaryResponse = jacksonConfiguration.objectMapper().readValue(result.getResponse().getContentAsString(), EmployeeSalary.class);
        assertEquals(employeeSalaryResponse.getGrossSalary(), grossSalary);
        assertEquals(employeeSalaryResponse.getNetSalary(), netSalary);

    }
}