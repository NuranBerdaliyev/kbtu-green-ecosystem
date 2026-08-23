ALTER TABLE vacancies
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_vacancies_active
    ON vacancies(is_active);

CREATE INDEX idx_containers_admin_monitoring
    ON eco_point_containers(is_active, fullness_percentage);