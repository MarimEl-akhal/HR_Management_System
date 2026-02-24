CREATE TABLE IF NOT EXISTS employee_expertise
(
    employee_id
        BIGINT
        NOT
            NULL,
    expertise_id
        BIGINT
        NOT
            NULL,
    PRIMARY
        KEY
        (
         employee_id,
         expertise_id
            ),
    CONSTRAINT fk_emp FOREIGN KEY
        (
         employee_id
            ) REFERENCES employees
            (
             employee_id
                ) ON DELETE CASCADE,
    CONSTRAINT fk_exp FOREIGN KEY
        (
         expertise_id
            ) REFERENCES expertises
            (
             expertise_id
                )
        ON DELETE CASCADE
);