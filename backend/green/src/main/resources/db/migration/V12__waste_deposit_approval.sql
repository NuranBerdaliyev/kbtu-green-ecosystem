ALTER TABLE waste_logs
    ADD COLUMN waste_type VARCHAR(20),
    ADD COLUMN fullness_delta_percentage INTEGER
        NOT NULL DEFAULT 0,
    ADD COLUMN status VARCHAR(20)
        NOT NULL DEFAULT 'APPROVED',
    ADD COLUMN reviewed_by_id BIGINT,
    ADD COLUMN reviewed_at TIMESTAMP;

ALTER TABLE waste_logs
    ALTER COLUMN waste_type SET NOT NULL,

ALTER COLUMN status SET DEFAULT 'PENDING';


ALTER TABLE waste_logs
    ADD CONSTRAINT fk_waste_logs_reviewed_by
        FOREIGN KEY (reviewed_by_id)
            REFERENCES users(id)
            ON DELETE SET NULL,

    ADD CONSTRAINT chk_waste_logs_type
        CHECK (
            waste_type IN (
                'PLASTIC',
                'BATTERY',
                'PAPER',
                'GLASS'
            )
        ),

    ADD CONSTRAINT chk_waste_logs_fullness_delta
        CHECK (
            fullness_delta_percentage
                BETWEEN 0 AND 100
        ),

    ADD CONSTRAINT chk_waste_logs_status
        CHECK (
            status IN (
                'PENDING',
                'APPROVED',
                'REJECTED'
            )
        );


CREATE INDEX idx_waste_logs_status_scanned
    ON waste_logs(status, scanned_at);
CREATE INDEX idx_waste_logs_user_approved_day
    ON waste_logs(user_id, status, reviewed_at);