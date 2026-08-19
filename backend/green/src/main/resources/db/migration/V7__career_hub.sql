CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    hr_manager_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    website VARCHAR(500),
    is_partner BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_companies_hr_manager
    FOREIGN KEY (hr_manager_id)
        REFERENCES users(id),

    CONSTRAINT uk_company_hr_name
        UNIQUE (hr_manager_id, name),

    CONSTRAINT chk_companies_name_not_blank
        CHECK (btrim(name) <> '')
);

CREATE INDEX idx_companies_hr_manager
    ON companies(hr_manager_id);


ALTER TABLE vacancies
    ADD COLUMN company_id BIGINT;



ALTER TABLE vacancies
    ALTER COLUMN company_id SET NOT NULL;


ALTER TABLE vacancies
    ADD CONSTRAINT fk_vacancies_company
        FOREIGN KEY (company_id)
            REFERENCES companies(id);


CREATE INDEX idx_vacancies_company
    ON vacancies(company_id);


ALTER TABLE vacancies
    DROP COLUMN company_name;

ALTER TABLE vacancies
    DROP COLUMN is_partner_vacancy;


CREATE INDEX idx_job_app_student
    ON job_applications(student_id);

CREATE INDEX idx_job_app_vacancy
    ON job_applications(vacancy_id);