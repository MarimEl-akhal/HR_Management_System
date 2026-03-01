package com.example.hrm_system.dto;

import com.example.hrm_system.enums.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UpdateEmployeeRequest {

    @JsonProperty("name")
    private JsonNullable<String> name;

    @JsonProperty("birthDate")
    private JsonNullable<LocalDate> birthDate;

    @JsonProperty("graduationDate")
    private JsonNullable<LocalDate> graduationDate;

    @JsonProperty("gender")
    private JsonNullable<Gender> gender;

    @JsonProperty("grossSalary")
    private JsonNullable<BigDecimal> grossSalary;

    @JsonProperty("managerId")
    private JsonNullable<Long> managerId;

    @JsonProperty("departmentId")
    private JsonNullable<Long> departmentId;

    @JsonProperty("teamId")
    private JsonNullable<Long> teamId;

    @JsonProperty("expertises")
    private JsonNullable<Set<String>> expertises;

}
