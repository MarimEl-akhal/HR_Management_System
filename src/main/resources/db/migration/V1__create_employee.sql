CREATE TABLE IF NOT EXISTS employees
(
    employee_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    birth_date      DATE         NOT NULL,
    graduation_date DATE         NOT NULL,
    gender          VARCHAR(255) NOT NULL,
    gross_salary    DOUBLE       NOT NULL,
    manager_id      BIGINT
);
