-- KBTU Green Ecosystem — MVP object seed
-- Run after Flyway migrations V1..V12.
-- DEV/DEMO ONLY. Password for every account: Demo1234
--
-- This seed creates only starting objects for the UI:
-- users, profiles, containers, companies, vacancies and trips.
-- All interactions must be performed through the frontend.

BEGIN;

SELECT pg_advisory_xact_lock(hashtext('kbtu-green-mvp-object-seed'));

-- Reset only previous @kbtu.demo / MVP-DEMO-* data.
-- Interactions created through the frontend are removed only when reseeding.

DELETE FROM job_applications
WHERE student_id IN (
    SELECT id FROM users WHERE email LIKE '%@kbtu.demo'
)
OR vacancy_id IN (
    SELECT v.id
    FROM vacancies v
    JOIN companies c ON c.id = v.company_id
    JOIN users u ON u.id = c.hr_manager_id
    WHERE u.email = 'hr@kbtu.demo'
);

DELETE FROM vacancies
WHERE hr_manager_id IN (
    SELECT id FROM users WHERE email = 'hr@kbtu.demo'
);

DELETE FROM companies
WHERE hr_manager_id IN (
    SELECT id FROM users WHERE email = 'hr@kbtu.demo'
);

DELETE FROM waste_logs
WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@kbtu.demo'
)
OR eco_point_container_id IN (
    SELECT id
    FROM eco_point_containers
    WHERE qr_code_token LIKE 'MVP-DEMO-%'
);

DELETE FROM eco_point_containers
WHERE qr_code_token LIKE 'MVP-DEMO-%';

DELETE FROM trip_participants
WHERE passenger_id IN (
    SELECT id FROM users WHERE email LIKE '%@kbtu.demo'
)
OR trip_id IN (
    SELECT t.id
    FROM trips t
    JOIN users u ON u.id = t.driver_id
    WHERE u.email = 'driver@kbtu.demo'
);

DELETE FROM trips
WHERE driver_id IN (
    SELECT id FROM users WHERE email = 'driver@kbtu.demo'
);

DELETE FROM eco_transactions
WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@kbtu.demo'
);

DELETE FROM user_achievements
WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@kbtu.demo'
);

DELETE FROM profiles
WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@kbtu.demo'
);

DELETE FROM authentications
WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@kbtu.demo'
);

DELETE FROM users
WHERE email LIKE '%@kbtu.demo';

-- Starting objects.

DO $mvp$
DECLARE
    -- BCrypt 2a, cost 10, password: Demo1234
    demo_password_hash CONSTANT VARCHAR(72) :=
        '$2a$10$knIgajdWY7c8liSnLDRRu.KAPVPhFAvCl65qpBG4984vLC53eCTaK';

    admin_id    BIGINT;
    hr_id       BIGINT;
    driver_id   BIGINT;
    student_id  BIGINT;
    student2_id BIGINT;
    employee_id BIGINT;

    partner_company_id BIGINT;
BEGIN
    -- Users are ready for login and role-based screens.
    -- Starting balances let students test paid carpool immediately.
    INSERT INTO users (
        email, full_name, role, eco_coins_balance, esg_rating,
        total_co2_saved, created_at, password_hash
    ) VALUES (
        'admin@kbtu.demo', 'Алия Администратор', 'ADMIN',
        0, 0, 0.000, CURRENT_TIMESTAMP, demo_password_hash
    ) RETURNING id INTO admin_id;

    INSERT INTO users (
        email, full_name, role, eco_coins_balance, esg_rating,
        total_co2_saved, created_at, password_hash
    ) VALUES (
        'hr@kbtu.demo', 'Диана HR', 'HR',
        0, 0, 0.000, CURRENT_TIMESTAMP, demo_password_hash
    ) RETURNING id INTO hr_id;

    INSERT INTO users (
        email, full_name, role, eco_coins_balance, esg_rating,
        total_co2_saved, created_at, password_hash
    ) VALUES (
        'driver@kbtu.demo', 'Арман Водитель', 'STUDENT',
        20, 28, 2.640, CURRENT_TIMESTAMP, demo_password_hash
    ) RETURNING id INTO driver_id;

    INSERT INTO users (
        email, full_name, role, eco_coins_balance, esg_rating,
        total_co2_saved, created_at, password_hash
    ) VALUES (
        'student@kbtu.demo', 'Нуран Экоактивист', 'STUDENT',
        50, 72, 9.250, CURRENT_TIMESTAMP, demo_password_hash
    ) RETURNING id INTO student_id;

    INSERT INTO users (
        email, full_name, role, eco_coins_balance, esg_rating,
        total_co2_saved, created_at, password_hash
    ) VALUES (
        'student2@kbtu.demo', 'Аружан Студент', 'STUDENT',
        35, 48, 4.850, CURRENT_TIMESTAMP, demo_password_hash
    ) RETURNING id INTO student2_id;

    INSERT INTO users (
        email, full_name, role, eco_coins_balance, esg_rating,
        total_co2_saved, created_at, password_hash
    ) VALUES (
        'employee@kbtu.demo', 'Ерлан Сотрудник', 'EMPLOYEE',
        25, 40, 3.200, CURRENT_TIMESTAMP, demo_password_hash
    ) RETURNING id INTO employee_id;

    INSERT INTO profiles (
        user_id, phone, avatar_url, bio, birth_date, updated_at
    ) VALUES
        (
            student_id, '+77010000001', NULL,
            'Участвую в совместных поездках и экологических инициативах кампуса.',
            DATE '2005-07-30', CURRENT_TIMESTAMP
        ),
        (
            student2_id, '+77010000002', NULL,
            'Студентка KBTU, интересуюсь аналитикой и устойчивым развитием.',
            DATE '2005-03-18', CURRENT_TIMESTAMP
        ),
        (
            driver_id, '+77010000003', NULL,
            'Езжу в KBTU и беру попутчиков по дороге.',
            DATE '2004-05-15', CURRENT_TIMESTAMP
        ),
        (
            hr_id, '+77010000004', NULL,
            'HR компании-партнёра KBTU Green Ecosystem.',
            DATE '1997-09-12', CURRENT_TIMESTAMP
        );

    -- No waste logs: deposits and approval happen through the frontend.
    INSERT INTO eco_point_containers (
        title, location, waste_type, fullness_percentage,
        capacity_grams, current_weight_grams, is_active, qr_code_token
    ) VALUES
        (
            'Главный холл — пластик',
            ST_SetSRID(ST_MakePoint(76.94570, 43.23640), 4326),
            'PLASTIC', 10, 5000, 500, TRUE, 'MVP-DEMO-PLASTIC'
        ),
        (
            'Библиотека — бумага',
            ST_SetSRID(ST_MakePoint(76.94635, 43.23682), 4326),
            'PAPER', 72, 10000, 7200, TRUE, 'MVP-DEMO-PAPER'
        ),
        (
            'IT Lab — батарейки',
            ST_SetSRID(ST_MakePoint(76.94515, 43.23595), 4326),
            'BATTERY', 92, 3000, 2760, TRUE, 'MVP-DEMO-BATTERY'
        ),
        (
            'Склад — стекло',
            ST_SetSRID(ST_MakePoint(76.94710, 43.23615), 4326),
            'GLASS', 0, 8000, 0, FALSE, 'MVP-DEMO-GLASS-INACTIVE'
        );

    -- One partner for immediate vacancy creation and one pending company
    -- for confirmation through the admin frontend.
    INSERT INTO companies (
        hr_manager_id, name, description, website, is_partner
    ) VALUES (
        hr_id,
        'GreenTech Kazakhstan',
        'Разрабатывает цифровые продукты для устойчивого города.',
        'https://example.com/greentech',
        TRUE
    ) RETURNING id INTO partner_company_id;

    INSERT INTO companies (
        hr_manager_id, name, description, website, is_partner
    ) VALUES (
        hr_id,
        'EcoLab Startup',
        'Новая компания ожидает подтверждения партнёрства.',
        'https://example.com/ecolab',
        FALSE
    );

    -- No applications: students apply through the frontend.
    INSERT INTO vacancies (
        hr_manager_id, company_id, title, description, is_active
    ) VALUES
        (
            hr_id,
            partner_company_id,
            'Java Backend Intern',
            'Разработка REST API на Java и Spring Boot, PostgreSQL и Docker.',
            TRUE
        ),
        (
            hr_id,
            partner_company_id,
            'Data Analyst Intern',
            'Анализ экологических метрик и визуализация данных.',
            TRUE
        );

    -- No participants or payment history. The published trip can be joined;
    -- the draft can be published by the driver through the frontend.

    RAISE NOTICE 'MVP object seed created. No interactions were inserted.';
END
$mvp$;

COMMIT;

-- Quick verification. The last three values must be zero.
SELECT email, role, eco_coins_balance, esg_rating
FROM users
WHERE email LIKE '%@kbtu.demo'
ORDER BY email;

SELECT
    (SELECT COUNT(*) FROM eco_point_containers WHERE qr_code_token LIKE 'MVP-DEMO-%')
        AS containers,
    (SELECT COUNT(*) FROM companies c JOIN users u ON u.id = c.hr_manager_id
        WHERE u.email = 'hr@kbtu.demo')
        AS companies,
    (SELECT COUNT(*) FROM vacancies v JOIN users u ON u.id = v.hr_manager_id
        WHERE u.email = 'hr@kbtu.demo')
        AS vacancies,
    (SELECT COUNT(*) FROM trips t JOIN users u ON u.id = t.driver_id
        WHERE u.email = 'driver@kbtu.demo')
        AS trips,
    (SELECT COUNT(*) FROM waste_logs wl JOIN users u ON u.id = wl.user_id
        WHERE u.email LIKE '%@kbtu.demo')
        AS waste_logs,
    (SELECT COUNT(*) FROM job_applications ja JOIN users u ON u.id = ja.student_id
        WHERE u.email LIKE '%@kbtu.demo')
        AS applications,
    (SELECT COUNT(*) FROM trip_participants tp JOIN users u ON u.id = tp.passenger_id
        WHERE u.email LIKE '%@kbtu.demo')
        AS participants;

-- Accounts (password for all: Demo1234)
-- admin@kbtu.demo
-- hr@kbtu.demo
-- driver@kbtu.demo
-- student@kbtu.demo
-- student2@kbtu.demo
-- employee@kbtu.demo
