ALTER TABLE eco_point_containers
    ADD COLUMN capacity_grams INTEGER NOT NULL DEFAULT 10000,
    ADD COLUMN current_weight_grams INTEGER NOT NULL DEFAULT 0;

ALTER TABLE eco_point_containers
    ADD CONSTRAINT chk_container_capacity_positive
        CHECK (capacity_grams > 0),

    ADD CONSTRAINT chk_container_current_weight_range
        CHECK (
            current_weight_grams >= 0
            AND current_weight_grams <= capacity_grams
        );

ALTER TABLE waste_logs
    ADD COLUMN waste_weight_grams INTEGER NOT NULL DEFAULT 0;

ALTER TABLE waste_logs
    ADD CONSTRAINT chk_waste_log_weight_non_negative
        CHECK (waste_weight_grams >= 0)