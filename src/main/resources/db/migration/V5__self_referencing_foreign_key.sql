ALTER Table employees
    ADD CONSTRAINT fk_employee_manager
        FOREIGN KEY (manager_id)
            REFERENCES employees (employee_id)
            ON DELETE SET NULL;