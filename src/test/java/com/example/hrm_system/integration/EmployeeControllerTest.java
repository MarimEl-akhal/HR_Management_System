package com.example.hrm_system.integration;

import com.example.hrm_system.dto.EmployeeResponse;
import com.example.hrm_system.entity.Employee;
import com.example.hrm_system.enums.Gender;
import com.example.hrm_system.repository.EmployeeRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

import static com.example.hrm_system.enums.ApiError.EMPLOYEE_NOT_FOUND;
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
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

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
