ALTER TABLE employees
    ADD COLUMN department_id BIGINT;


ALTER TABLE employees
    ADD COLUMN team_id BIGINT;


ALTER TABLE employees
    ADD CONSTRAINT fk_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id)
            ON DELETE SET NULL;


ALTER TABLE employees
    ADD CONSTRAINT fk_team
        FOREIGN KEY (team_id) REFERENCES teams (team_id)
            ON DELETE SET NULL;
