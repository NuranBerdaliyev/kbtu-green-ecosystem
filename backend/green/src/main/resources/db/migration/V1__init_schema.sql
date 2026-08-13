CREATE EXTENSION IF NOT EXISTS postgis;
--SQL for initializing database
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    eco_coins_balance BIGINT NOT NULL,
    esg_rating INTEGER NOT NULL,
    total_co2_saved NUMERIC(15,3) NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_email_not_blank CHECK (btrim(email) <> ''),
    CONSTRAINT chk_users_full_name_not_blank CHECK (btrim(full_name) <> ''),
    CONSTRAINT chk_users_role_values CHECK (role IN ('STUDENT', 'EMPLOYEE', 'HR', 'ADMIN')),
    CONSTRAINT chk_users_eco_coins_non_negative CHECK (eco_coins_balance >= 0),
    CONSTRAINT chk_users_esg_rating_range CHECK (esg_rating BETWEEN 0 AND 100),
    CONSTRAINT chk_users_total_co2_non_negative CHECK (total_co2_saved >= 0)
);

CREATE TABLE trips (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    departure_location geometry(Point,4326) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    total_seats INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,
    trip_status VARCHAR(20) NOT NULL,

    CONSTRAINT fk_trips_driver FOREIGN KEY (driver_id) REFERENCES users(id),
    CONSTRAINT chk_trips_total_seats_positive CHECK (total_seats > 0),
    CONSTRAINT chk_trips_available_seats_non_negative CHECK (available_seats >= 0),
    CONSTRAINT chk_trips_available_le_total CHECK (available_seats <= total_seats),
    CONSTRAINT chk_trips_status_values CHECK (trip_status IN ('CREATED', 'ACTIVE', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE trip_participants (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    is_cancelled BOOLEAN NOT NULL,

    CONSTRAINT fk_trip_participants_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_participants_passenger FOREIGN KEY (passenger_id) REFERENCES users(id),
    CONSTRAINT uk_trip_passenger UNIQUE (trip_id, passenger_id)
);

CREATE TABLE eco_point_containers (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    location geometry(Point,4326) NOT NULL,
    waste_type VARCHAR(20) NOT NULL,
    fullness_percentage INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL,
    qr_code_token VARCHAR(255) NOT NULL,

    CONSTRAINT uk_eco_point_containers_qr UNIQUE (qr_code_token),
    CONSTRAINT chk_containers_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT chk_containers_qr_not_blank CHECK (btrim(qr_code_token) <> ''),
    CONSTRAINT chk_containers_waste_type_values CHECK (waste_type IN ('PLASTIC', 'BATTERY', 'PAPER', 'GLASS')),
    CONSTRAINT chk_containers_fullness_range CHECK (fullness_percentage BETWEEN 0 AND 100)
);

CREATE TABLE waste_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    eco_point_container_id BIGINT NOT NULL,
    scanned_at TIMESTAMP NOT NULL,
    eco_coins_earned INTEGER NOT NULL,

    CONSTRAINT fk_waste_logs_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_waste_logs_container FOREIGN KEY (eco_point_container_id) REFERENCES eco_point_containers(id),
    CONSTRAINT chk_waste_logs_eco_coins_non_negative CHECK (eco_coins_earned >= 0)
);

CREATE TABLE vacancies (
    id BIGSERIAL PRIMARY KEY,
    hr_manager_id BIGINT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    is_partner_vacancy BOOLEAN NOT NULL,

    CONSTRAINT fk_vacancies_hr_manager FOREIGN KEY (hr_manager_id) REFERENCES users(id),
    CONSTRAINT chk_vacancies_company_not_blank CHECK (btrim(company_name) <> ''),
    CONSTRAINT chk_vacancies_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT chk_vacancies_description_not_blank CHECK (btrim(description) <> '')
);

CREATE TABLE job_applications (
    id BIGSERIAL PRIMARY KEY,
    vacancy_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    applied_at TIMESTAMP NOT NULL,
    cover_letter TEXT NOT NULL,
    job_status VARCHAR(20) NOT NULL,

    CONSTRAINT fk_job_applications_vacancy FOREIGN KEY (vacancy_id) REFERENCES vacancies(id),
    CONSTRAINT fk_job_applications_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT uk_job_application_vacancy_student UNIQUE (vacancy_id, student_id),
    CONSTRAINT chk_job_applications_cover_letter_len CHECK (char_length(cover_letter) BETWEEN 10 AND 5000),
    CONSTRAINT chk_job_applications_status_values CHECK (job_status IN ('PENDING', 'REVIEWED', 'ACCEPTED', 'REJECTED'))
);

CREATE INDEX idx_trips_status_time ON trips (trip_status, departure_time);
CREATE INDEX idx_container_waste_type ON eco_point_containers (waste_type);
CREATE INDEX idx_job_app_status ON job_applications (job_status);